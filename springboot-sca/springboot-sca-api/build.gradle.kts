// 共享 API：Feign 服务契约（UserApi 接口）+ 模型（User），供 provider / consumer 复用。
// 本模块是纯库，不是 Spring Boot 应用。根项目对所有子模块套了 spring-boot 插件——
// api 无主类，若不禁用 bootJar，构建时 bootJar 任务会因为找不到主类而报错。故在此关闭
// bootJar，仅产出普通 jar 供 provider/consumer 以 project(:...) 依赖引入。
// starter-web（提供 Spring Web 注解：@GetMapping 等，Feign 契约要用）与 lombok 由根项目统一提供。
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}
tasks.named<Jar>("jar") {
    enabled = true
}
