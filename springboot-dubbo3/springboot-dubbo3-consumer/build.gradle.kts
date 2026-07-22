// 服务②：Dubbo Triple 消费者（@DubboReference 调 provider 的 UserService），用 Spring MVC
// controller 暴露 /dubbo/users/** 以便 curl 触发「原生 Triple RPC」验证。
// 通用依赖（spring-boot-starter-web、lombok）、打包（spring-boot）、Java 版本由根项目统一提供。
// dubbo-spring-boot-starter（非 ...starter3）面向 Spring Boot 2.x / Java 8；仅 SB3 需 JDK17。
// dubbo-nacos-spring-boot-starter 传递引入 nacos-client 2.x（gRPC 走 9848），与 rnacos(Nacos 2.x) 兼容。
dependencies {
    add("implementation", project(":springboot-dubbo3-api"))
    add("implementation", "org.apache.dubbo:dubbo-spring-boot-starter:3.3.0")
    add("implementation", "org.apache.dubbo:dubbo-nacos-spring-boot-starter:3.3.0")
}
