package com.examples.nats.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examples.nats.producer.NatsProducer;

/**
 * 触发 NATS 发布的测试接口。调用后由后台 Dispatcher 异步接收并打印日志，以此验证收发。
 */
@RestController
public class NatsController {

    private final NatsProducer producer;

    public NatsController(NatsProducer producer) {
        this.producer = producer;
    }

    /** 发到普通发布订阅 subject：所有订阅者都会收到。 */
    @GetMapping("/nats/publish")
    public String publish(@RequestParam(defaultValue = "hello") String msg) {
        producer.publishPubSub(msg);
        return "published (pub/sub) [springboot.nats.pubsub]: " + msg + "\n";
    }

    /** 发到队列组 subject：同一队列组内负载均衡，只投给一个成员。 */
    @GetMapping("/nats/publishQueue")
    public String publishQueue(@RequestParam(defaultValue = "task") String msg) {
        producer.publishQueue(msg);
        return "published (queue group) [springboot.nats.queue]: " + msg + "\n";
    }
}
