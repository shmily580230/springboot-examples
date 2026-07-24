import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.springframework.boot.gradle.tasks.run.BootRun

// 根项目：统一版本 + 所有子模块通用的依赖与打包配置。
// 版本由插件版本（apply false）决定：spring-boot 插件版本即 Spring Boot BOM 版本。
// 注意：apply false 不能去掉，否则 spring-boot 会应用到根项目（聚合器，无主类）→ 根 bootJar 必崩。
plugins {
    id("org.springframework.boot") version "2.7.18" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "com.examples"
    version = "1.0-SNAPSHOT"

    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }
}

// 以下配置对所有子模块通用（根项目是聚合器，不参与）
subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    // 通用依赖（所有子模块共享）；版本由 Spring Boot BOM 自动决定
    dependencies {
        add("implementation", "org.springframework.boot:spring-boot-starter-web")
        add("compileOnly", "org.projectlombok:lombok")
        add("annotationProcessor", "org.projectlombok:lombok")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OpenTelemetry（Java Agent 方式）：给两个 SCA 服务（provider/consumer）接 OTel。
// 用 OTel Java Agent（-javaagent）零代码埋点：自动覆盖 consumer 的 Spring MVC（HTTP 入口）
// → Feign client span → HTTP → provider 的 Spring MVC server span，靠 W3C traceparent
// 串成一条跨进程链路，OTLP gRPC 上报到本地 OpenObserve（:5081）。
//
// agent jar 入库仓库根（sca 服务共用一份）；downloadOtelAgent 仅作兜底：jar 缺失时才下载。
// 仅作用于两个可运行的 SCA 服务（api 是纯 jar，不需要）；版本与 springboot-otel 模块的
// opentelemetry-spring-boot-starter:2.28.1 对齐——仓库只留一个 OTel 版本号。
val otelAgentVersion = "2.28.1"
val otelAgentJar = rootProject.file("opentelemetry-javaagent.jar") // 仓库级共享（sca 服务共用一份）

// 在根项目注册一次下载任务，两个服务的 bootRun 都依赖它（避免重复下载同一文件）。
tasks.register("downloadOtelAgent") {
    outputs.file(otelAgentJar) // jar 已存在则 up-to-date，自动跳过 doLast
    doLast {
        if (!otelAgentJar.exists()) {
            val url = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v$otelAgentVersion/opentelemetry-javaagent.jar"
            logger.lifecycle("Downloading OpenTelemetry Java Agent v$otelAgentVersion -> ${otelAgentJar.path}")
            otelAgentJar.parentFile.mkdirs()
            java.net.URL(url).openStream().use { input ->
                otelAgentJar.outputStream().use { input.copyTo(it) }
            }
        }
    }
}

// 给 sca 的 provider / consumer 的 bootRun 挂载 agent + OTLP 上报（traces-only；metrics/logs 见 README 可选开关）。
// 显式 project(modulePath) 而非 configure(list){...}：Kotlin DSL 下 configure 的 lambda 无隐式 receiver，
// 直接取各项目更可靠，且 tasks.named 是惰性的，spring-boot 插件迟早会注册 bootRun。
listOf(
    ":springboot-sca-provider", ":springboot-sca-consumer", ":springboot-feign-reactor"
).forEach { modulePath ->
    project(modulePath).tasks.named<BootRun>("bootRun") {
        dependsOn(rootProject.tasks.named("downloadOtelAgent"))
        jvmArgs(
            "-javaagent:${otelAgentJar.absolutePath}",
            "-Dotel.service.name=${project(modulePath).name}",                       // 各服务取自身模块名
            "-Dotel.exporter.otlp.protocol=grpc",
            "-Dotel.exporter.otlp.endpoint=http://localhost:5081",                   // OpenObserve gRPC
            "-Dotel.exporter.otlp.headers=Authorization=Basic YWRtaW5AYWRtaW4uY29tOkFkbWluITEyMw==,organization=default,stream-name=default",
            "-Dotel.traces.sampler=parentbased_always_on",                           // 演示用：100% 采样，保证每次 curl 都能看到链路
            "-Dotel.metrics.exporter=none",                                          // 不要 metrics
            "-Dotel.logs.exporter=none"                                              // 不要 logs（否则 agent 默认把 Nacos/Spring 内部日志也上报，淹没真正想看的 trace span）
        )
    }
}
