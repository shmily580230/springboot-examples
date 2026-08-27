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

// gRPC（devh grpc-spring-boot-starter + Nacos 服务发现 + protobuf codegen + OTel）：api（.proto 契约/生成 stub）+ provider（服务①）+ consumer（服务②）
// 与 sca 同构：三模块收纳在 springboot-grpc/ 下；projectDir 重定向保持模块名不变。devh 2.15.x / SCA 2021.0.x ↔ Spring Boot 2.7（JDK8）。
include("springboot-grpc-api")
include("springboot-grpc-provider")
include("springboot-grpc-consumer")
project(":springboot-grpc-api").projectDir = file("springboot-grpc/springboot-grpc-api")
project(":springboot-grpc-provider").projectDir = file("springboot-grpc/springboot-grpc-provider")
project(":springboot-grpc-consumer").projectDir = file("springboot-grpc/springboot-grpc-consumer")
