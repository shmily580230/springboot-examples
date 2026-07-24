package com.examples.feign.reactor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactivefeign.ReactiveFeignBuilder;
import reactivefeign.spring.config.AbstractReactiveFeignConfigurator;
import reactivefeign.spring.config.ReactiveFeignNamedContext;

/**
 * 通过 {@code AbstractReactiveFeignConfigurator} 向 feign-reactor 注册请求/响应日志拦截器。
 * <p>
 * feign-reactor 的自动配置会扫描所有 {@code AbstractReactiveFeignConfigurator} Bean，
 * 并调用 {@code configure()} 方法，从而将自定义拦截器注入到构建链中。
 */
@Configuration
public class FeignConfig {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    public AbstractReactiveFeignConfigurator feignLoggingConfigurator() {
        return new AbstractReactiveFeignConfigurator(0) {

            @Override
            public ReactiveFeignBuilder configure(ReactiveFeignBuilder builder,
                                                   ReactiveFeignNamedContext context) {
                return builder.addExchangeFilterFunction(new FeignLoggingInterceptor());
            }
        };
    }
}
