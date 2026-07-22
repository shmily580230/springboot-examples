pluginManagement {
    repositories {
        // 阿里云公共仓库
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "springboot-examples"

include("springboot-otel")
include("springboot-nats")

// Dubbo 3 Triple + REST：api（共享接口/模型）+ provider（服务①）+ consumer（服务②）
// 三模块统一收纳在 springboot-dubbo3/ 下；用 projectDir 重定向保持模块名不变，
// 这样依赖 project(":springboot-dubbo3-api") 与 :springboot-dubbo3-*:bootRun 写法都不受影响。
include("springboot-dubbo3-api")
include("springboot-dubbo3-provider")
include("springboot-dubbo3-consumer")
project(":springboot-dubbo3-api").projectDir = file("springboot-dubbo3/springboot-dubbo3-api")
project(":springboot-dubbo3-provider").projectDir = file("springboot-dubbo3/springboot-dubbo3-provider")
project(":springboot-dubbo3-consumer").projectDir = file("springboot-dubbo3/springboot-dubbo3-consumer")
