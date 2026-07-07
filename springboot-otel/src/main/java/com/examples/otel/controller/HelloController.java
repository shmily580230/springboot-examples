package com.examples.otel.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 测试接口：每次调用产生 1 条 HTTP 服务端 span（starter 自动埋点）+ 1 条自定义子 span + 1 个自定义计数器自增，
 * 并在异步线程里打印一条日志（携带当前 trace 上下文，使日志带上同一 trace_id）。
 */
@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    private static final int ASYNC_POOL_SIZE = 4;

    private final Tracer tracer;
    private final LongCounter requestCounter;

    // 异步多线程池：固定 4 个守护线程，命名 otel-async-1/2/3/4（编号由 CustomizableThreadFactory 内部维护）
    private final ExecutorService asyncExecutor =
            Executors.newFixedThreadPool(ASYNC_POOL_SIZE, daemonThreadFactory("otel-async-"));

    /** 守护线程工厂：复用 Spring 的 CustomizableThreadFactory 做命名，并设为守护线程。 */
    private static ThreadFactory daemonThreadFactory(String prefix) {
        CustomizableThreadFactory factory = new CustomizableThreadFactory(prefix);
        factory.setDaemon(true);
        return factory;
    }

    public HelloController(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("springboot-otel");
        this.requestCounter = openTelemetry.getMeter("springboot-otel")
                .counterBuilder("otel_hello_requests_total")
                .setDescription("Total /hello requests")
                .build();
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        asyncExecutor.shutdown();
        asyncExecutor.awaitTermination(2, TimeUnit.SECONDS);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "otel") String name) throws InterruptedException {
        log.info("hello requested: name={}", name);

        requestCounter.add(1);

        Span span = tracer.spanBuilder("doWork").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("hello.name", name);
            Thread.sleep(50); // 模拟耗时，便于在链路里观察到该 span

            // 异步线程打印日志：捕获当前 OTel 上下文（含 trace），在新线程里 makeCurrent，
            // 这样日志的 MDC 会带上同一 trace_id/span_id（日志-链路关联）。
            Context parentContext = Context.current();
            String asyncName = name;
            asyncExecutor.submit(() -> {
                try (Scope asyncScope = parentContext.makeCurrent()) {
                    log.info("async work done: name={}", asyncName);
                }
            });
        } finally {
            span.end();
        }

        log.info("hello done: name={}", name);
        return "hello " + name + "\n";
    }

    /** 触发一条 ERROR 日志，用于验证错误日志文件（springboot-otel-error.log）。 */
    @GetMapping("/error")
    public String error() {
        log.error("simulated error for testing the error log file");
        return "error logged\n";
    }
}
