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

- **超时**：`WebReactiveFeign$Builder` 构造时无条件套用 `WebReactiveOptions.DEFAULT_OPTIONS`，即使不写任何配置也会生效，默认值为：

  | 配置项 | 默认 | 作用层（reactor-netty） |
  |---|---|---|
  | `connect-timeout-millis` | `5000`（5s） | `ChannelOption.CONNECT_TIMEOUT_MILLIS`，TCP 连接建立超时 |
  | `read-timeout-millis` | `10000`（10s） | `ReadTimeoutHandler`，**单次**读空闲超时（两次读之间） |
  | `write-timeout-millis` | `10000`（10s） | `WriteTimeoutHandler`，**单次**写超时 |
  | `response-timeout-millis` | **未启用**（`null`） | `HttpClient.responseTimeout`，整个请求-响应超时；默认不设，需要时手动开启 |

  > 注意：read/write 是 per-read/per-write 的 handler 级超时，不是"整个响应耗时上限"。要限制一次调用的总时长，用 `response-timeout-millis`。

  **覆盖时的坑**：`reactive.feign.client.config.<name>.options` 是**整体替换**而非合并——`ReactiveFeignBasicConfigurator` 会调 `optionsBuilder.build()` 生成一个全新 `WebReactiveOptions`，yaml 里没写的字段在 builder 中为 `null`，**不会回退到 `DEFAULT_OPTIONS` 的 5s/10s/10s**。所以要么四项都写全，要么只补想改的、其余接受退化为不启用：

  ```yaml
  reactive:
    feign:
      client:
        config:
          jsonplaceholder:
            options:
              connect-timeout-millis: 3000   # 改为 3s
              read-timeout-millis: 5000      # 改为 5s
              write-timeout-millis: 10000    # 想保留默认就得显式写
              response-timeout-millis: 8000  # 默认 null，这里开启整体 8s 超时
  ```
