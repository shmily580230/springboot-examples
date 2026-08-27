package com.examples.grpc.provider;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供者的 HTTP 健康端点（8094）。
 * <p>
 * 这个 Tomcat 的存在有两层意义：一是 spring-cloud-commons 的自动注册只由 {@code WebServerInitializedEvent}
 * 触发（javap AbstractAutoServiceRegistration 可证），没有 HTTP server 就不会注册进 Nacos；
 * 二是给 k8s readiness/liveness 之类的探针一个业务流量（gRPC）之外的检查面。
 */
@RestController
public class HealthController {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    /** GET /health：探活 + 顺带暴露 gRPC 端口，便于人肉核对。 */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("status", "UP");
        info.put("grpcPort", grpcPort);
        return info;
    }
}
