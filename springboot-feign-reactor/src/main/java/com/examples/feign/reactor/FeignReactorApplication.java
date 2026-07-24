package com.examples.feign.reactor;

import com.examples.feign.reactor.config.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactivefeign.spring.config.EnableReactiveFeignClients;

/**
 * 响应式 Feign 客户端演示：{@code @ReactiveFeignClient} 基于 WebClient 非阻塞调用远端 API。
 * <p>
 * {@link EnableReactiveFeignClients @EnableReactiveFeignClients}：扫描并注册所有
 * {@code @ReactiveFeignClient} 接口为 Spring Bean。
 */
@EnableReactiveFeignClients(defaultConfiguration = FeignConfig.class)
@SpringBootApplication
public class FeignReactorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignReactorApplication.class, args);
    }
}
