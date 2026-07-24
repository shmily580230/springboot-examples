# springboot-feign-reactor

基于 [PlaytikaOSS/feign-reactive](https://github.com/PlaytikaOSS/feign-reactive) (`3.2.11`) 的响应式 Feign 客户端演示。

## 是什么

`ReactiveFeignClient` 将 OpenFeign 的声明式 API 风格与 Spring WebClient 的**非阻塞 HTTP 客户端**结合：
- 方法返回 `Mono<T>` / `Flux<T>`，底层走 Netty 事件循环，不阻塞 Tomcat 线程
- 请求映射复用 Spring Web 注解（`@GetMapping`、`@PathVariable` 等）
- 与 `@FeignClient` 可以共存，本模块演示纯响应式用法

## 模块架构

```
controller (FeignReactorController, @RestController)
    → client (JsonPlaceholderClient, @ReactiveFeignClient)
        → WebClient → JSONPlaceholder (https://jsonplaceholder.typicode.com)
```

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `feign-reactor-spring-cloud-starter:3.2.11` | 一键引入：配置自动注册 + WebClient 实现 + Cloud 支持 |
| `feign-slf4j` | Feign 日志桥接到 SLF4J |
| `spring-cloud-dependencies:2021.0.5` | Spring Cloud BOM（版本兼容性由 feign-reactive 3.2.11 → SC 2021.0.6 决定） |

> 选 3.2.11 而非 4.x/5.x：3.2.11 的 `feign-reactor-parent` 明确 `java.version=1.8` + `spring-boot-dependencies=2.6.14` + `spring-cloud=2021.0.6`，与本仓库（SB 2.7.18 / Java 8）兼容。

## 启动

```bash
./gradlew :springboot-feign-reactor:bootRun
```

## 验证

```bash
# 获取全部 posts（Flux，流式返回）
curl http://localhost:8083/posts

# 按 ID 获取单条 post（Mono）
curl http://localhost:8083/posts/1
```

## 设计要点

- **不引入 `spring-boot-starter-webflux`**：只需 WebClient（HTTP 客户端），不需要 Netty server。Tomcat 照常处理 HTTP 请求，MVC 控制器返回 `Mono`/`Flux` 由 Spring MVC 2.7 原生支持。
- **`url` 直连**：`@ReactiveFeignClient(url = "...")` 跳过服务发现，适合演示和对固定第三方 API 的调用。生产环境可以只用 `name` + `spring.cloud.loadbalancer` 做客户端负载均衡。
- **请求/响应日志 + 耗时**：通过 `FeignLoggingInterceptor`（`ReactiveHttpExchangeFilterFunction`）拦截，启动后每次调用会输出：
  ```
  Feign request: GET https://jsonplaceholder.typicode.com/posts, headers={...}
  Feign response: GET https://jsonplaceholder.typicode.com/posts -> status=200, headers={...}, elapsed=123ms
  ```
  logback 的 `LOG_PATTERN` 已包含 `[%X{trace_id:-}] [%X{span_id:-}]` —— 接入 OTel Agent 后日志自动携带链路信息。

- **超时**：默认 `connectTimeoutMillis=5s`、`readTimeoutMillis=10s`、`writeTimeoutMillis=10s`。可按客户端（`name`）单独覆盖：
  ```yaml
  reactive:
    feign:
      client:
        config:
          jsonplaceholder:
            options:
              connect-timeout-millis: 3000
              read-timeout-millis: 5000
              response-timeout-millis: 8000
  ```
