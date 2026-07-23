# springboot-sca（Spring Cloud Alibaba：OpenFeign + Nacos + OTel）

Spring Boot 2.7 + Spring Cloud Alibaba（2021.0.x）示例：用 **Nacos 服务发现 + OpenFeign** 做 HTTP 微服务调用——

- **provider**：普通 Spring MVC `@RestController`，注册到 Nacos（service name = `sca-provider`）。
- **consumer**：`@FeignClient` 按 service name 经 Nacos 负载均衡以 HTTP 调 provider。
- 两个服务都挂 **OTel Java Agent**，把 `consumer → provider` 串成一条跨进程链路上报到 OpenObserve。

> 经典 Spring Cloud 微服务形态：**HTTP/JSON**（Feign）做服务间调用，区别于二进制 RPC。

拆成 3 个 Gradle 模块（= 2 个可运行服务 + 1 个共享库）：

| 模块 | 类型 | 作用 |
|---|---|---|
| `springboot-sca-api` | 纯 jar（非服务） | `UserApi` 契约（Spring Web 注解）+ `User` 模型，provider/consumer 共享 |
| `springboot-sca-provider` | 服务① | `@RestController implements UserApi`，`8091`，注册 Nacos |
| `springboot-sca-consumer` | 服务② | `@FeignClient extends UserApi` + 触发用 MVC `8092` |

## 版本对应（关键）

SCA 2021.0.x ↔ Spring Cloud 2021.0.x ↔ Spring Boot 2.6/2.7（JDK8）。本模块用：

- `spring-cloud-dependencies:2021.0.5`
- `spring-cloud-alibaba-dependencies:2021.0.5.0`

> **Feign 必须配 `spring-cloud-starter-loadbalancer`**：2021.x 起 OpenFeign 走 Spring Cloud LoadBalancer（非 Netflix Ribbon），不显式引入会在调用时无 LB 报错。

## 前置：需要一个 Nacos 注册中心

provider/consumer 连 `127.0.0.1:8848`。需要一个 **Nacos 2.x** 兼容的服务端：

```bash
docker run -d --name rnacos -p 8848:8848 -p 9848:9848 -p 10848:10848 qingpan/rnacos:stable
```

> 与 nats 模块一样，**Nacos 未启动时服务启动会失败**——请先启动 Nacos。

## 运行

需要**两个终端**分别起 provider 和 consumer（先 provider 后 consumer）：

```bash
# 终端 1：provider（8091，OTel agent 自动挂载）
./gradlew :springboot-sca-provider:bootRun

# 终端 2：consumer（8092，OTel agent 自动挂载）
./gradlew :springboot-sca-consumer:bootRun
```

## 验证

```bash
curl http://localhost:8092/sca/users/1
# {"id":1,"name":"user-1","email":"user1@example.com"}

curl -X POST http://localhost:8092/sca/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"alice","email":"alice@example.com"}'
# {"id":1784...,"name":"alice","email":"alice@example.com"}
```

也可直接打 provider（不经 Feign）：

```bash
curl http://localhost:8091/users/1
# {"id":1,"name":"user-1","email":"user1@example.com"}
```

## OpenTelemetry（Java Agent 方式）

两个服务都挂 OTel Java Agent（共用**仓库根**的 `opentelemetry-javaagent.jar`，已随仓库内置；缺失时 `bootRun` 会自动补下载）。链路：

```
curl :8092/sca/users/1
  consumer: GET /sca/users/{id} (Spring MVC server span)
           └ Feign GET sca-provider/users/{id} (Feign client span)
  provider:   GET /users/{id} (Spring MVC server span)   ← 同 traceId，W3C traceparent 透传
```

OTLP gRPC 上报到本地 OpenObserve（`localhost:5081`，与 `springboot-otel` 同一后端）。
日志里每行会带 `[trace=...,span=...]`（agent 注入 MDC），可与 trace 对账。

agent 挂载与 OTLP 参数写在根 `build.gradle.kts`（`bootRun` 的 `jvmArgs`），**默认只要 traces**
（`-Dotel.metrics.exporter=none` + `-Dotel.logs.exporter=none`）；想开 metrics/logs 改对应 `-Dotel.*.exporter` 即可。

## Feign 调优（生产级配置）

consumer 对 Feign 做了几项常规调优（见 `application.yml`）：

| 项 | 配置 | 作用 |
|---|---|---|
| OkHttp 连接池 | `feign.okhttp.enabled=true` + 依赖 `feign-okhttp` | 替换默认 JDK `HttpURLConnection`，连接复用、稳态更快 |
| 显式超时 | `feign.client.config.default.connect-timeout/read-timeout` | 连接 2s / 读 5s（默认偏长且不透明） |
| BASIC 日志 | `feign.client.config.default.logger-level=basic` + logback 把 `UserFeignClient` logger 调到 DEBUG | 每次调用打印：方法/URI/状态码/耗时 |

实测稳态 ~5ms。**首请求有一次冷启开销**（LoadBalancer 首次实例解析 + 首条 TCP 连接，本机约 0.4~1s）——这是一次性的；生产里通常由 readiness probe + 少量预热流量在基础设施层消化，不在应用代码里做 dummy 调用。

> ⚠️ **命名空间坑（重要）**：SC OpenFeign **3.1.x（= 2021.0.x）用的是 `feign.*`**，不是 `spring.cloud.openfeign.*`
> （后者是 4.0 / 2022.0 才改的）。验证：jar 里 `FeignClientProperties = @ConfigurationProperties("feign.client")`、
> OkHttp 走 `feign.okhttp.enabled`。**前缀写错会被静默忽略**（OkHttp 不生效、Feign 日志不输出，且无任何报错）——
> 这是个很容易踩的坑，配置后务必用 BASIC 日志确认 `---> GET ...` 真的打出来了。

## 设计说明

- **共享契约接口**：`UserApi` 只带 Spring Web 注解、不带 `@FeignClient`。provider 用 `@RestController implements UserApi` 自动继承映射；consumer 用 `@FeignClient(name="sca-provider") extends UserApi` 复用同一组路径。这样契约只有一份。
- **触发前缀 `/sca/users/**`**：刻意区别于 Feign 契约的 `/users/**`，便于在 trace 里区分 consumer 入口与 provider 处理。
- **`@EnableDiscoveryClient` + `@EnableFeignClients`**：consumer 侧两个注解都开；provider 只开 DiscoveryClient。
