package com.examples.feign.reactor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.client.log.ReactiveLoggerListener;
import feign.MethodMetadata;
import feign.Target;

/**
 * 自定义 feign-reactor 日志监听器：输出请求信息、响应状态、body 及耗时。
 */
@Slf4j
public class FeignLoggerListener implements ReactiveLoggerListener<FeignLoggerListener.Context> {

    @Override
    public Context requestStarted(ReactiveHttpRequest request, Target<?> target, MethodMetadata metadata) {
        log.info("Feign request: {} {}, headers={}",
                request.method(), request.uri(), request.headers());
        return new Context();
    }

    @Override
    public boolean logRequestBody() {
        return true;
    }

    @Override
    public void bodySent(Object body, Context context) {
        String json = toJson(body);
        log.info("Feign request body, length={}, body={}", json.length(), json);
    }

    @Override
    public void responseReceived(ReactiveHttpResponse<?> response, Context context) {
        long elapsed = System.currentTimeMillis() - context.startTime;
        log.info("Feign response: {} {} -> elapsed={}ms, status={}",
                response.request().method(), response.request().uri(), elapsed, response.status());
    }

    @Override
    public void errorReceived(Throwable error, Context context) {
        long elapsed = System.currentTimeMillis() - context.startTime;
        log.error("Feign error: {} {} -> elapsed={}ms, error={}",
                error.getMessage(), elapsed, error);
    }

    @Override
    public boolean logResponseBody() {
        return true;
    }

    @Override
    public void bodyReceived(Object body, Context context) {
        String json = toJson(body);
        log.info("Feign response body, length={}, body={}", json.length(), json);
    }

    static class Context {
        final long startTime = System.currentTimeMillis();
    }

    private static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
}
