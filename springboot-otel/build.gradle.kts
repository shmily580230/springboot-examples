// 本模块专属：OpenTelemetry（OTLP 上报）。
// 通用依赖（spring-boot-starter-web、lombok）、打包（spring-boot）、Java 版本均由根项目统一提供。
dependencies {
    add("implementation", "io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:2.28.1")
}
