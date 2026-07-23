package com.examples.sca.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Spring Cloud Alibaba 消费者启动类。
 * <p>
 * {@link EnableDiscoveryClient @EnableDiscoveryClient}：从 Nacos 发现 provider。
 * {@link EnableFeignClients @EnableFeignClients}：启用 @FeignClient 接口注入，
 * consumer 经 Feign + Spring Cloud LoadBalancer 以 HTTP 调用 {@code sca-provider}。
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class ScaConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaConsumerApplication.class, args);
    }
}
