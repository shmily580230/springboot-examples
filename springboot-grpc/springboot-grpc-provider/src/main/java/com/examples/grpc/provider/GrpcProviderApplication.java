package com.examples.grpc.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * gRPC 提供者启动类。
 * <p>
 * 引入 devh {@code grpc-server-spring-boot-starter} 后，标 {@code @GrpcService} 的 Bean 会被自动注册到
 * 独立的 Netty gRPC server（端口由 {@code grpc.server.port} 配置，见 application.yml），
 * 无需手写 {@code ServerBuilder} 生命周期。对比 sca-provider：那里是 Tomcat + Spring MVC。
 * <p>
 * {@link EnableDiscoveryClient @EnableDiscoveryClient} 把本服务注册到 Nacos（service name = grpc-provider）：
 * 实例本身挂在 HTTP 8094（触发注册 + 健康检查），gRPC 端口 9090 经注册 metadata {@code gRPC.port} 告知 consumer
 * （devh 的 DiscoveryClientNameResolver 优先读该 metadata）。
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GrpcProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcProviderApplication.class, args);
    }
}
