package com.examples.feign.reactor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactivefeign.client.log.ReactiveLoggerListener;

/**
 * feign-reactor 默认配置：注册自定义日志监听器，替换内置 DefaultReactiveLogger。
 */
@Configuration
public class FeignConfig {

    @Bean
    public ReactiveLoggerListener<?> reactiveLogger() {
        return new FeignLoggerListener();
    }
}
