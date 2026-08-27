// 服务②：gRPC 消费者——devh starter 管理 Channel，@GrpcClient 注入；
// 寻址走 Nacos（discovery:///grpc-provider + 注册 metadata gRPC.port），
// 并用 Spring MVC controller 暴露 /grpc/users/** 以便 curl 触发验证。
// devh 2.15.x ↔ Spring Boot 2.7（JDK8）；grpc 版本（1.58.0）由 api 模块传递（devh 内置 grpc-bom:1.58.0）。
// SCA 2021.0.x ↔ Spring Cloud 2021.0.x ↔ Spring Boot 2.6/2.7（JDK8）：与 sca 模块同套 BOM。
// 注意：devh 的 DiscoveryClientNameResolver 用 grpc-java 自带 LB（round_robin），
// 不经 Spring Cloud LoadBalancer，所以不像 sca-consumer 那样要加 loadbalancer 依赖。
dependencies {
    add("implementation", platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    add("implementation", platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.0.5.0"))
    add("implementation", project(":springboot-grpc-api"))
    add("implementation", "net.devh:grpc-client-spring-boot-starter:2.15.0.RELEASE")
    add("implementation", "com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
}
