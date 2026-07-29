package com.examples.feign.reactor.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import feign.MethodMetadata;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.client.log.ReactiveLoggerListener;

import java.nio.charset.StandardCharsets;

/**
 * 自定义 feign-reactor 日志监听器：输出请求信息、响应状态、body 及耗时。
 */
@Slf4j
public class FeignLoggerListener implements ReactiveLoggerListener<Long> {

    @Override
    public Long requestStarted(ReactiveHttpRequest request, Target<?> target, MethodMetadata metadata) {
        log.info("Feign request: {} {}, headers={}",
                request.method(), request.uri(), request.headers());
        return System.currentTimeMillis();
    }

    @Override
    public boolean logRequestBody() {
        return true;
    }

    @Override
    public void bodySent(Object body, Long startTime) {
        String json = JSONUtil.toJsonStr(Convert.toStr(body));
        log.info("Feign request body, length={}, body={}", json.length(), json);
    }

    @Override
    public void responseReceived(ReactiveHttpResponse<?> response, Long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Feign response: elapsed={}ms, status={}", elapsed, response.status());
    }

    @Override
    public void errorReceived(Throwable error, Long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        log.error("Feign error: elapsed={}ms, error={}", elapsed, error.getMessage());
    }

    @Override
    public boolean logResponseBody() {
        return true;
    }

    @Override
    public void bodyReceived(Object body, Long startTime) {
        // 响应体可能是 byte[]、String 或已解码对象（Map/POJO）：
        // byte[]/String 直接按文本输出，避免被当作数字数组序列化；
        // 对象按 JSON 格式化输出，避免打印成 {k=v} 的 toString 形式
        String text;
        if (body instanceof byte[]) {
            text = new String((byte[]) body, StandardCharsets.UTF_8);
        } else if (body instanceof String) {
            text = (String) body;
        } else {
            text = JSONUtil.toJsonStr(body);
        }
        log.info("Feign response body, length={}, body={}", text.length(), text);
    }
}
