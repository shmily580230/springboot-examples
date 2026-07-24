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
include("springboot-feign-reactor")

// Spring Cloud Alibaba（OpenFeign + Nacos + OTel）：api（共享接口/模型）+ provider（服务①）+ consumer（服务②）
// 三模块统一收纳在 springboot-sca/ 下；用 projectDir 重定向保持模块名不变，
// 这样依赖 project(":springboot-sca-api") 写法不受影响。SCA 2021.0.x ↔ Spring Cloud 2021.0.x ↔ SB 2.6/2.7（JDK8）。
include("springboot-sca-api")
include("springboot-sca-provider")
include("springboot-sca-consumer")
project(":springboot-sca-api").projectDir = file("springboot-sca/springboot-sca-api")
project(":springboot-sca-provider").projectDir = file("springboot-sca/springboot-sca-provider")
project(":springboot-sca-consumer").projectDir = file("springboot-sca/springboot-sca-consumer")
