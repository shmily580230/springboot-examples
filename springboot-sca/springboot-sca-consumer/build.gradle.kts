// 服务②：Spring Cloud Alibaba 消费者——@FeignClient 经 Nacos 负载均衡以 HTTP 调 provider，
// 并用 Spring MVC controller 暴露 /sca/users/** 以便 curl 触发验证。
// 通用依赖（spring-boot-starter-web、lombok）、打包（spring-boot）、Java 版本由根项目统一提供。
// SCA 2021.0.x ↔ Spring Cloud 2021.0.x ↔ Spring Boot 2.6/2.7（JDK8）：用 Gradle 原生 platform() 导入 BOM
// 管版本（不依赖 dependency-management 插件，避免在子脚本里 import 插件类型）。
dependencies {
    add("implementation", platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    add("implementation", platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.0.5.0"))
    add("implementation", project(":springboot-sca-api"))
    add("implementation", "com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
    add("implementation", "org.springframework.cloud:spring-cloud-starter-openfeign")
    // 2021.x 起 Feign 走 Spring Cloud LoadBalancer（非 Netflix Ribbon）：需显式引入，否则无 LB 报错
    add("implementation", "org.springframework.cloud:spring-cloud-starter-loadbalancer")
    // OkHttp 连接池：替换默认 JDK HttpURLConnection，连接复用、稳态调用更快（版本由 spring-cloud BOM 管）
    add("implementation", "io.github.openfeign:feign-okhttp")
}
