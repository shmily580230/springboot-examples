package com.examples.nats;

/**
 * 演示用 NATS subject / 队列组常量。生产者与消费者共用，避免字面量散落各处。
 */
public final class NatsSubjects {

    private NatsSubjects() {
    }

    /** 普通发布订阅 subject：所有订阅者都会收到每条消息（广播）。 */
    public static final String PUB_SUB = "springboot.nats.pubsub";

    /** 队列组 subject：同一队列组内的订阅者负载均衡（每条消息只投给一个成员）。 */
    public static final String QUEUE = "springboot.nats.queue";

    /**
     * 队列组名称：多个实例订阅相同 subject + 相同队列组名时，消息才会在它们之间负载均衡。
     */
    public static final String QUEUE_GROUP = "springboot-nats-workers";
}
