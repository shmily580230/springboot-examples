# springboot-otel

Spring Boot 2.7 + OpenTelemetry 示例：通过 OTLP gRPC 上报链路（traces）/ 指标（metrics）。

OTLP 配置见 `src/main/resources/application.yml`（endpoint / headers / insecure）。

## 前置：需要一个 OTLP 接收端
本模块把数据上报到 `http://localhost:4317`（gRPC 明文）。请先在本地起一个 OTLP 接收端，
例如 OpenTelemetry Collector：

```bash
# 示例：用官方 otelcol 监听 4317（按你的后端改写 exporters）
otelcol --config=otel-collector.yaml
```

## 运行
```bash
./gradlew :springboot-otel:bootRun
```

## 验证数据
```bash
curl 'http://localhost:8081/hello?name=otel'
# => hello otel
```
每次调用会产生：
- 1 条 HTTP 服务端 span（starter 自动埋点）
- 1 条自定义子 span `doWork`
- 1 个自定义计数器 `otel_hello_requests_total` 自增

到你的 OTLP 后端查看 service=`springboot-otel` 的链路与指标即可确认上报生效。
若 4317 不可达，应用照常运行，日志里会出现 OTLP 连接失败的告警。
