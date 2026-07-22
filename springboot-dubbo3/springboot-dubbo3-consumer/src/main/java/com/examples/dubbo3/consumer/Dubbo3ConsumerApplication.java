package com.examples.dubbo3.consumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dubbo Triple 消费者启动类。
 * <p>
 * {@link EnableDubbo @EnableDubbo} 开启 @DubboReference 注入；配合 application.yml：
 * server.port=8083（Spring MVC 触发器）、registry=nacos。
 */
@EnableDubbo
@SpringBootApplication
public class Dubbo3ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dubbo3ConsumerApplication.class, args);
    }
}
