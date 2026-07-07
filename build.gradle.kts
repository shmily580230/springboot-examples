import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile

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
