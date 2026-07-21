package com.examples.nats.consumer;

import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.examples.nats.NatsSubjects;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;

/**
 * NATS 消息消费者（收）。启动时创建两个 {@link Dispatcher}，分别演示：
 * <ol>
 *   <li>普通订阅——所有订阅者都收到每条消息（广播）。</li>
 *   <li>队列组订阅——同一队列组内负载均衡（每条消息只投给一个成员）。</li>
 * </ol>
 */
@Component
public class NatsConsumer {

    private static final Logger log = LoggerFactory.getLogger(NatsConsumer.class);

    private final Connection natsConnection;

    private Dispatcher pubSubDispatcher;
    private Dispatcher queueDispatcher;

    public NatsConsumer(Connection natsConnection) {
        this.natsConnection = natsConnection;
    }

    @PostConstruct
    public void subscribe() {
        // nats-spring starter 在 server 未配置时返回 null 的 Connection；这里防御一下，避免 NPE。
        if (natsConnection == null) {
            log.warn("NATS Connection 为空（server 未配置？），跳过订阅");
            return;
        }

        // 1) 普通订阅：所有订阅者都收
        pubSubDispatcher = natsConnection.createDispatcher(this::onPubSub);
        pubSubDispatcher.subscribe(NatsSubjects.PUB_SUB);

        // 2) 队列组订阅：同组负载均衡
        queueDispatcher = natsConnection.createDispatcher(this::onQueue);
        queueDispatcher.subscribe(NatsSubjects.QUEUE, NatsSubjects.QUEUE_GROUP);

        log.info("已订阅: [{}]（普通订阅）; [{}] 队列组 [{}]（负载均衡）",
                NatsSubjects.PUB_SUB, NatsSubjects.QUEUE, NatsSubjects.QUEUE_GROUP);
    }

    private void onPubSub(Message msg) {
        log.info("[普通订阅] 收到 [{}]: {}", msg.getSubject(), decode(msg.getData()));
    }

    private void onQueue(Message msg) {
        log.info("[队列组 {}] 收到 [{}]: {}", NatsSubjects.QUEUE_GROUP, msg.getSubject(), decode(msg.getData()));
    }

    private static String decode(byte[] data) {
        return data == null ? "" : new String(data, StandardCharsets.UTF_8);
    }

    @PreDestroy
    public void shutdown() {
        if (natsConnection == null) {
            return;
        }
        // 关闭 dispatcher 即取消订阅；Connection 本身由 starter 管理生命周期（Spring 推断其 close() 销毁方法）。
        if (pubSubDispatcher != null) {
            natsConnection.closeDispatcher(pubSubDispatcher);
        }
        if (queueDispatcher != null) {
            natsConnection.closeDispatcher(queueDispatcher);
        }
        log.info("已关闭 NATS 订阅 dispatcher");
    }
}
