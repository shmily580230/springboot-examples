package com.examples.sca.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Cloud Alibaba 提供者启动类。
 * <p>
 * {@link EnableDiscoveryClient @EnableDiscoveryClient} 把本服务注册到 Nacos（service name = sca-provider），
 * 供 consumer 的 @FeignClient 经 service name 发现并负载均衡调用。
 * 本身是普通 Spring MVC：{@code UserController implements UserApi}。
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ScaProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaProviderApplication.class, args);
    }
}
