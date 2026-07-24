// feign-reactive 模块：基于 Spring WebClient 的响应式 Feign 客户端。
// @ReactiveFeignClient 方法返回 Mono/Flux，调用 jsonplaceholder 作为演示后端。
// 通用依赖（spring-boot-starter-web、lombok）、打包、Java 版本由根项目统一提供。
dependencies {
    add("implementation", "com.playtika.reactivefeign:feign-reactor-spring-cloud-starter:3.2.11")
    add("runtimeOnly", "io.netty:netty-resolver-dns-native-macos:4.1.101.Final:osx-aarch_64")
}
