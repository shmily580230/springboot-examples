// 服务①：gRPC 提供者——devh starter 起 Netty gRPC server（9090），
// @GrpcService 的 UserGrpcService 实现 api 模块 codegen 出的 UserServiceGrpc stub 契约；
// 并注册到 Nacos（service name = grpc-provider，metadata 带 gRPC.port=9090 供 consumer 的 discovery 解析）。
// devh 2.15.x ↔ Spring Boot 2.7（JDK8）；grpc 版本（1.58.0）由 api 模块传递（devh 内置 grpc-bom:1.58.0）。
// SCA 2021.0.x ↔ Spring Cloud 2021.0.x ↔ Spring Boot 2.6/2.7（JDK8）：与 sca 模块同套 BOM，用 Gradle 原生 platform() 导入。
dependencies {
    add("implementation", platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    add("implementation", platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.0.5.0"))
    add("implementation", project(":springboot-grpc-api"))
    add("implementation", "net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")
    add("implementation", "com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
}
