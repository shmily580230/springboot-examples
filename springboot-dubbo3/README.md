# springboot-dubbo3（Triple 协议 + REST，双服务）

Spring Boot 2.7 + Dubbo 3.3 示例：用 **Triple（tri）协议**同时承载两种调用——

1. **原生 Triple RPC**：consumer 经 Nacos 发现 provider，跨进程发起 Triple 二进制 RPC（`@DubboReference`）。
2. **REST（HTTP/JSON）**：同一个服务接口加 Spring Web 注解后，由 Triple 直接对外暴露 REST，curl 即可访问。

拆成 3 个 Gradle 模块（= 2 个可运行服务 + 1 个共享库）：

| 模块 | 类型 | 作用 |
|---|---|---|
| `springboot-dubbo3-api` | 纯 jar（非服务） | `UserService` 接口 + `User` 模型，provider/consumer 共享 |
| `springboot-dubbo3-provider` | 服务① | `@DubboService` 实现，Triple `50052`，注册 Nacos，对外 REST |
| `springboot-dubbo3-consumer` | 服务② | `@DubboReference` + Spring MVC `8083`，触发原生 RPC |

## 前置：需要一个 Nacos 注册中心

provider/consumer 连 `nacos://127.0.0.1:8848`。需要一个 **Nacos 2.x** 兼容的服务端（本仓库用 `rnacos`，也可用官方 `nacos-server`）：

```bash
# 方式 A：rnacos（Rust 实现，协议兼容 Nacos 2.x，更轻）
docker run -d --name rnacos -p 8848:8848 -p 9848:9848 -p 10848:10848 qingpan/rnacos:stable
# 8848=主端口(HTTP) 9848=gRPC 10848=rnacos 自带控制台(Dubbo 客户端用不到)

# 方式 B：官方 nacos-server
docker run -d --name nacos -p 8848:8848 -p 9848:9848 -e MODE=standalone nacos/nacos-server:v2.3.0
```

> **端口坑**：Nacos 2.x 客户端除了主端口 **8848**，还会用 **gRPC 端口 9848**（= 8848 + 1000，由 SDK 自动推导，**不要**把 9848 写进 `registry.address`）。docker 必须同时暴露两者，否则连不上。

> 与 nats 模块一样，**Nacos 未启动时 provider 启动会失败**——请先启动 Nacos。

## 运行

需要**两个终端**分别起 provider 和 consumer（先 provider 后 consumer）：

```bash
# 终端 1：provider（Triple 50052，无 Tomcat）
./gradlew :springboot-dubbo3-provider:bootRun

# 终端 2：consumer（Spring MVC 8083）
./gradlew :springboot-dubbo3-consumer:bootRun
```

## 验证（四条 curl）

```bash
# ── 路径① REST over Triple：直接打 provider 的 Triple 端口（不经 Spring MVC）──
curl http://localhost:50052/users/1
# {"id":1,"name":"user-1","email":"user1@example.com"}

curl -X POST http://localhost:50052/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"alice","email":"alice@example.com"}'
# {"id":1784...,"name":"alice","email":"alice@example.com"}

# ── 路径② 原生 Triple RPC：打 consumer 的 controller，内部走 @DubboReference → Nacos → provider ──
curl http://localhost:8083/dubbo/users/1
# {"id":1,"name":"user-1","email":"user1@example.com"}

curl -X POST http://localhost:8083/dubbo/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"carol","email":"carol@example.com"}'
# {"id":1784...,"name":"carol","email":"carol@example.com"}
```

路径① 与 路径② 命中同一个 `UserServiceImpl`，但**链路不同**：① 是 Triple 自己的 HTTP/JSON 映射；② 是 Spring MVC → Dubbo 二进制 RPC。consumer 的 controller 刻意用 `/dubbo/users/**` 前缀以示区分。

## 踩坑 / 实现说明

- **`User` 必须实现 `Serializable`**：REST 走 JSON 不需要，但「原生 Triple RPC」走 hessian2，且 Dubbo 3.3 默认开启**序列化安全严格检查**——非 Serializable 的类会被拒绝序列化。不实现的话：请求/响应序列化失败，create 直接报错；getById 则因 provider 发不出响应，consumer 表现为「等待 3s 超时」（`DEADLINE_EXCEEDED`），容易被误判成网络问题。
- **REST 路径要写在方法上**：Dubbo 3.3 Triple REST 在「类级 `@RequestMapping("/users")` + 方法空路径」时，会把空路径解析成**方法名**（`create()` → `/users/create`），导致标准的 `POST /users`（基路径）404。因此本例把完整路径写在每个方法上（`@GetMapping("/users/{id}")`、`@PostMapping("/users")`），拿到标准 REST 形态。
- **provider 关闭 web 上下文**（`spring.main.web-application-type=none`）：provider 是纯 Dubbo 提供者，REST 由 Triple 自带 Netty 在 50052 提供，不需要 Tomcat。
- **多网卡机器**：Dubbo 会自动探测本机 IP 注册到 Nacos（如同时有 VPN/utun、容器 bridge 时可能选到非预期网卡）。本机示例下 provider/consumer 同机，探测到的地址互相可达，无需额外配置。

## 依赖说明

- `org.apache.dubbo:dubbo-spring-boot-starter:3.3.0`（非 `...starter3`）：面向 Spring Boot 2.x / Java 8；`...starter3` 才是 SB3（需 JDK17）。
- `org.apache.dubbo:dubbo-nacos-spring-boot-starter:3.3.0`：Nacos 注册中心集成，传递引入 nacos-client 2.x。
- `spring-boot-starter-web`（根项目统一提供）：给 api 模块的 Spring Web 注解、consumer 的 Spring MVC 用。
- Triple 协议 + 原生 REST 支持已含在 `dubbo` 核心，无需额外 REST 依赖。
