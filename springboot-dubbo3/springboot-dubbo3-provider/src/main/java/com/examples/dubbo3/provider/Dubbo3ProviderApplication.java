package com.examples.dubbo3.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dubbo Triple 提供者启动类。
 * <p>
 * {@link EnableDubbo @EnableDubbo} 开启 @DubboService / @DubboReference 的扫描与导出。
 * 配合 application.yml：protocol=tri:50052、registry=nacos、web-application-type=none。
 */
@EnableDubbo
@SpringBootApplication
public class Dubbo3ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dubbo3ProviderApplication.class, args);
    }
}
