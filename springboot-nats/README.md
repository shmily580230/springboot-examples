# springboot-nats

Spring Boot 2.7 + NATS 示例：用官方 `io.nats:nats-spring-boot-starter` 自动配置 `Connection`，
演示**发布订阅**与**队列组（负载均衡）**两种消息收发模式。

- 发：REST 接口触发 `NatsProducer` 往 subject 发布消息。
- 收：`NatsConsumer` 启动时用 `Dispatcher` 订阅，收到消息打印日志。

## 前置：需要一个 NATS Server
本模块连接 `nats://localhost:4222`。先用 Docker 起一个 NATS：

```bash
docker run -d --name nats -p 4222:4222 -p 8222:8222 nats
# 4222 = 客户端端口；8222 = 监控端口（可选，访问 http://localhost:8222/varz 看状态）
```

> 注意：与 otel 模块不同，NATS 是核心依赖。**若 NATS 未启动，应用会在启动阶段抛连接异常而启动失败**——请先启动 NATS 再 `bootRun`。

## 运行
```bash
./gradlew :springboot-nats:bootRun
```

## 验证消息收发
```bash
# 1) 普通发布订阅（广播）
curl 'http://localhost:8082/nats/publish?msg=hello'
# 应用日志：[普通订阅] 收到 [springboot.nats.pubsub]: hello

# 2) 队列组（负载均衡）
curl 'http://localhost:8082/nats/publishQueue?msg=task1'
# 应用日志：[队列组 springboot-nats-workers] 收到 [springboot.nats.queue]: task1
```

## 队列组负载均衡演示
队列组的特性：同一个 `subject + queue group name` 的多个订阅者之间，每条消息只投递给其中一个。
单实例看不出差异，需要起第二个实例：

```bash
# 另起一个实例（换端口，与 8082 实例共用同一队列组 springboot-nats-workers）
./gradlew :springboot-nats:bootRun --args='--server.port=8083'

# 连续发多条 queue 消息，观察 8082 / 8083 两个实例的日志轮流收到（负载均衡）
for i in 1 2 3 4 5 6; do curl "http://localhost:8082/nats/publishQueue?msg=t$i"; done
```

## 依赖说明
`io.nats:nats-spring-boot-starter:0.5.7` 是官方 NATS Spring Boot starter，对齐 Spring Boot 2.7 / Java 8，
传递引入 jnats 2.17.2。更新的 0.6.x 面向 Spring Boot 3.x，与本仓库（2.7.18 / Java 8）不兼容。
配置前缀为 `nats.spring.*`（如 `nats.spring.server`、`nats.spring.connection-name`、`nats.spring.max-reconnect`）。
