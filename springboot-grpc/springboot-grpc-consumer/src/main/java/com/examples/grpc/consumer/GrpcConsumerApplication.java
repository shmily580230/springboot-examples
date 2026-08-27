package com.examples.grpc.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * gRPC 消费者启动类。
 * <p>
 * 引入 devh {@code grpc-client-spring-boot-starter} 后，标 {@code @GrpcClient("...")} 的字段/方法
 * 由 BeanPostProcessor 注入（client name 对应 application.yml 里 {@code grpc.client.<name>.*} 的 Channel 配置）。
 * 本服务同时是普通 Spring MVC 应用（8093），暴露 /grpc/users/** 用 curl 触发 gRPC 调用。
 * <p>
 * {@link EnableDiscoveryClient @EnableDiscoveryClient}：client name 的地址写成 {@code discovery:///grpc-provider}
 * 后，devh 经 Nacos 拿 provider 实例列表（metadata gRPC.port 拨对端口），LB 用 grpc-java 的 round_robin。
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GrpcConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcConsumerApplication.class, args);
    }
}
