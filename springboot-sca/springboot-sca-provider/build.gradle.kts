// 服务①：Spring Cloud Alibaba 提供者——普通 Spring MVC @RestController（implements UserApi），
// 经 Nacos 注册为 sca-provider，被 consumer 的 @FeignClient 经 Nacos 负载均衡以 HTTP 调用。
// 通用依赖（spring-boot-starter-web、lombok）、打包（spring-boot）、Java 版本由根项目统一提供。
// SCA 2021.0.x ↔ Spring Cloud 2021.0.x ↔ Spring Boot 2.6/2.7（JDK8）：用 Gradle 原生 platform() 导入 BOM
// 管版本（不依赖 dependency-management 插件，避免在子脚本里 import 插件类型）。
dependencies {
    add("implementation", platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    add("implementation", platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.0.5.0"))
    add("implementation", project(":springboot-sca-api"))
    add("implementation", "com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
}
