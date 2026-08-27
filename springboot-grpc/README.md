# springboot-grpc（gRPC + Nacos 服务发现 + OTel）

Spring Boot 2.7 示例：用 **gRPC（HTTP/2 + protobuf 二进制协议）** 做微服务调用，**Nacos 做服务发现**——与 `springboot-sca` 同构的一对 provider/consumer，对照着看正好理解两种 RPC 风格的差异：

| | sca（OpenFeign） | 本模块（gRPC） |
|---|---|---|
| 契约 | Java 接口 + Spring Web 注解 | `.proto` IDL → codegen 生成 stub |
| 传输 | HTTP/1.1 + JSON（文本） | HTTP/2 + protobuf（二进制） |
| 服务端 | `@RestController implements UserApi` | `@GrpcService extends UserServiceGrpc.UserServiceImplBase` |
| 客户端 | `@FeignClient extends UserApi` | `@GrpcClient` 注入 Channel → `newBlockingStub` |
| 服务发现 | Nacos + Spring Cloud LoadBalancer | Nacos + grpc-java 自带 LB（round_robin） |
| 端口协商 | 实例端口即业务端口 | 实例注册 HTTP 端口，gRPC 端口走注册 metadata `gRPC.port` |

- **provider**：devh starter 起独立 Netty gRPC server（`9090`），实现 `user.UserService`；注册 Nacos（`grpc-provider`，实例挂 HTTP `8094`）。
- **consumer**：普通 Spring MVC（`8093`）暴露 `/grpc/users/**`，收到 HTTP 后按 service name 经 Nacos 发现 provider，gRPC 调用。
- 两个服务都挂 **OTel Java Agent**，把 `consumer → provider` 串成一条跨进程链路上报到 OpenObserve（W3C traceparent 经 gRPC metadata 透传）。

拆成 3 个 Gradle 模块（= 2 个可运行服务 + 1 个共享库），与 sca 完全同构：

| 模块 | 类型 | 作用 |
|---|---|---|
| `springboot-grpc-api` | 纯 jar（非服务） | `user.proto` 契约 + codegen 出的 message/stub，provider/consumer 共享 |
| `springboot-grpc-provider` | 服务① | `@GrpcService UserGrpcService`，gRPC `9090` + HTTP `8094`（注册/探活） |
| `springboot-grpc-consumer` | 服务② | `@GrpcClient` 注入 Channel + 触发用 MVC `8093` |

## 版本对应（关键）

- devh `grpc-spring-boot-starter` 2.x ↔ Spring Boot 2.x（JDK8）；3.x 才是 Boot 3/JDK17。本模块用：
  - `net.devh:grpc-{server,client}-spring-boot-starter:2.15.0.RELEASE`（2.x 末版，内置 `grpc-bom:1.58.0`）
  - `io.grpc:*:1.58.0` + `protobuf-java:3.24.4`（与 protoc 3.24.4 同线，grpc 1.58 传递 3.24.0，靠 Gradle 取高者对齐）
  - `com.google.protobuf` Gradle 插件 `0.9.5`：protoc / protoc-gen-grpc-java 二进制按平台（osx-aarch_64 等）自动从 maven 下载
- SCA `2021.0.5.0` ↔ Spring Cloud `2021.0.5` ↔ Spring Boot 2.6/2.7（JDK8）：与 sca 模块同套 BOM

## 前置：需要一个 Nacos 注册中心

provider/consumer 连 `127.0.0.1:8848`（与 sca 共用同一个）。需要一个 **Nacos 2.x** 兼容的服务端：

```bash
docker run -d --name rnacos -p 8848:8848 -p 9848:9848 -p 10848:10848 qingpan/rnacos:stable
```

> 与 sca 模块一样，**Nacos 未启动时服务启动会失败**——请先启动 Nacos。

## 运行

需要**两个终端**分别起 provider 和 consumer（先 provider 后 consumer）：

```bash
# 终端 1：provider（gRPC 9090 + HTTP 8094，OTel agent 自动挂载）
./gradlew :springboot-grpc-provider:bootRun

# 终端 2：consumer（MVC 8093，OTel agent 自动挂载）
./gradlew :springboot-grpc-consumer:bootRun
```

> 首次构建会下载 protoc / protoc-gen-grpc-java 平台二进制（几十 MB），之后走缓存。

## 验证

```bash
curl http://localhost:8093/grpc/users/1
# {"id":1,"name":"user-1","email":"user1@example.com"}   ← 经 Nacos 发现 provider 后 gRPC 调用

curl -X POST http://localhost:8093/grpc/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"alice","email":"alice@example.com"}'
# {"id":1787...,"name":"alice","email":"alice@example.com"}
```

辅助核对（不经 consumer，直接看 provider 两侧）：

```bash
curl http://localhost:8094/health          # {"status":"UP","grpcPort":9090}（provider 的 HTTP 面）
curl "http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=grpc-provider"
# 实例 port=8094，metadata 里带 "gRPC.port":"9090"（consumer 就靠它拨对端口）
grpcurl -plaintext -d '{"id":1}' localhost:9090 user.UserService/GetUser   # 直接打 gRPC（需装 grpcurl）
```

## Nacos 服务发现（gRPC 的两个特殊点）

gRPC 接入 Nacos 后，与 Feign/HTTP 有两处不同，都出在「端口」上：

1. **实例端口 ≠ gRPC 端口**。provider 注册进 Nacos 的实例是 HTTP `8094`，业务流量却在 gRPC `9090`。
   解法是注册 metadata：`spring.cloud.nacos.discovery.metadata."gRPC.port": 9090`——devh 的
   `DiscoveryClientNameResolver` 优先读该键拨 gRPC 端口（实测它还会自动补一个 legacy 键 `gRPC_port`，
   由 `GrpcMetadataNacosConfiguration` 自动写入，两者都认）。
2. **provider 必须保留一个 HTTP 端口**。spring-cloud-commons 的自动注册只由 `WebServerInitializedEvent`
   触发（`javap AbstractAutoServiceRegistration` 可证），`web-application-type: none` 的纯 gRPC 服务
   **永远不会注册进 Nacos**。所以 provider 留了 Tomcat（8094）：触发注册 + 充当探活面，业务流量不走它。

consumer 侧寻址：

```yaml
grpc:
  client:
    grpc-provider:                      # client name = @GrpcClient("grpc-provider")
      address: discovery:///grpc-provider        # 注意是三斜杠：discovery:/// + service name
      default-load-balancing-policy: round_robin # 默认 pick_first 只连一个实例，多实例要显式开
```

> LB 差异：Feign 走 Spring Cloud LoadBalancer（所以 sca-consumer 要显式加 loadbalancer 依赖）；
> devh 的 resolver 是 grpc-java 的 NameResolver，LB 用 grpc 自带的（`round_robin`），**不需要** loadbalancer 依赖。

## OpenTelemetry（Java Agent 方式）

与 sca 完全同一套机制（共用仓库根的 `opentelemetry-javaagent.jar`，挂载写在根 `build.gradle.kts`）。链路：

```
curl :8093/grpc/users/1
  consumer: GET /grpc/users/{id} (Spring MVC server span)
           └ gRPC user.UserService/GetUser (grpc client span)
  provider:   user.UserService/GetUser (grpc server span)   ← 同 traceId，traceparent 走 gRPC metadata
```

OTel agent 对 grpc-java 是原生零代码埋点（client/server 两侧都是），span 名即全方法名 `user.UserService/GetUser`。

## gRPC 形态速记（对比 Spring MVC）

- **响应不 return**：往 `StreamObserver` 里 `onNext(消息)` + `onCompleted()`，异常走 `onError(t)`。
- **stub 按 Channel 现造**：`UserServiceGrpc.newBlockingStub(channel)`——stub 轻量不可变，Channel 才是贵的（devh 按 client name 管理连接池）。
- **consumer 的 JSON 边界要自己做映射**：protobuf 对象不能直接交给 Jackson（会把它全部 getter 序列化出来），所以 consumer 里有 `proto User → POJO User` 的一层。
- **proto 即契约**：字段编号（`id = 1`）一旦发布不可改含义——兼容性靠「只加不改不删」，这是与 Java 接口契约最大的思维差异。

## 设计说明

- **`java_multiple_files = true`**：每个 message 生成独立 `.java`（`User.java` 而非 `UserProto.User`），业务侧引用自然。
- **provider 的 HealthController**：Tomcat 的存在是功能性的（触发注册），顺带提供 `/health`，这是 gRPC-only 服务上 k8s 的常规形态（探针走 HTTP，业务走 gRPC）。
- **keep-alive**：consumer 对 Channel 开了 TCP keep-alive（`enable-keep-alive` 等），长连接不被中间设备静默掐断——gRPC 长连接下的常规配置。
- **api 模块用 `java-library` 的 `api` 依赖**：生成 stub 的公开类型直接引用 grpc/protobuf 类，需要把它们暴露到 consumer 的编译期 classpath（普通 `implementation` 依赖会靠 Maven 传递侥幸编译过，显式声明更稳）。
