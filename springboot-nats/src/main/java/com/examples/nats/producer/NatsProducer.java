package com.examples.nats.producer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.examples.nats.NatsSubjects;

import io.nats.client.Connection;

/**
 * NATS 消息生产者（发）。复用 nats-spring starter 自动配置的 {@link Connection}。
 */
@Component
public class NatsProducer {

    private static final Logger log = LoggerFactory.getLogger(NatsProducer.class);

    /** publish 是异步投递，flush 确保消息真正发出；超时仅告警，不阻断业务（消息仍会在重连后投递）。 */
    private static final Duration FLUSH_TIMEOUT = Duration.ofSeconds(1);

    private final Connection natsConnection;

    public NatsProducer(Connection natsConnection) {
        this.natsConnection = natsConnection;
    }

    /** 发到普通发布订阅 subject：所有订阅者都会收到。 */
    public void publishPubSub(String message) {
        publish(NatsSubjects.PUB_SUB, message);
    }

    /** 发到队列组 subject：同组内负载均衡，每条消息只投给一个成员。 */
    public void publishQueue(String message) {
        publish(NatsSubjects.QUEUE, message);
    }

    /** 发到任意 subject。 */
    public void publish(String subject, String message) {
        natsConnection.publish(subject, message.getBytes(StandardCharsets.UTF_8));
        try {
            natsConnection.flush(FLUSH_TIMEOUT);
        } catch (Exception e) {
            log.warn("flush NATS 失败（消息仍会尝试投递）: {}", e.getMessage());
        }
        log.info("已发布到 [{}]: {}", subject, message);
    }
}
