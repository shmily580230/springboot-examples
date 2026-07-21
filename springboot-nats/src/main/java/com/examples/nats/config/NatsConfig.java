package com.examples.nats.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;

/**
 * 自定义 NATS 连接/错误监听器，覆盖 nats-spring starter 提供的默认实现。
 * starter 默认监听器只打印英文 INFO；这里换成 SLF4J，使连接状态变化与异常进入本应用的日志体系。
 * <p>
 * 这两个 Bean 在 starter 中是 {@code @ConditionalOnMissingBean}，这里一旦定义即取代默认实现。
 */
@Configuration
public class NatsConfig {

    private static final Logger log = LoggerFactory.getLogger(NatsConfig.class);

    @Bean
    public ConnectionListener connectionListener() {
        // 连接状态变化（如 CONNECTED / DISCONNECTED / RECONNECTED）触发
        return (conn, type) -> log.info("NATS 连接状态变化: {} (当前状态: {})", type, conn.getStatus());
    }

    @Bean
    public ErrorListener errorListener() {
        return new ErrorListener() {
            @Override
            public void slowConsumerDetected(Connection conn, Consumer consumer) {
                log.warn("NATS 慢消费者告警: {}", consumer);
            }

            @Override
            public void exceptionOccurred(Connection conn, Exception exp) {
                log.error("NATS 处理异常", exp);
            }

            @Override
            public void errorOccurred(Connection conn, String error) {
                log.error("NATS 连接错误: {}", error);
            }
        };
    }
}
