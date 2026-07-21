// 本模块专属：NATS 消息收发（官方 io.nats:nats-spring-boot-starter，对齐 Spring Boot 2.7 / Java 8）。
// 通用依赖（spring-boot-starter-web、lombok）、打包（spring-boot）、Java 版本均由根项目统一提供。
// 选 0.5.7 而非 0.6.x：0.6.x 面向 Spring Boot 3.x，与本仓库（2.7.18 / Java 8）不兼容；0.5.7 传递引入 jnats 2.17.2。
dependencies {
    add("implementation", "io.nats:nats-spring-boot-starter:0.5.7")
}
