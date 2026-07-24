package com.examples.feign.reactor.config;

import lombok.extern.slf4j.Slf4j;
import reactivefeign.client.ReactiveHttpClient;
import reactivefeign.client.ReactiveHttpExchangeFilterFunction;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpResponse;
import reactor.core.publisher.Mono;

/**
 * feign-reactor 请求/响应日志拦截器，打印请求信息、响应状态及耗时。
 */
@Slf4j
public class FeignLoggingInterceptor implements ReactiveHttpExchangeFilterFunction<Mono<?>> {

    @Override
    public Mono<ReactiveHttpResponse<Mono<?>>> filter(ReactiveHttpRequest request,
                                                       ReactiveHttpClient<Mono<?>> next) {
        long start = System.currentTimeMillis();

        log.info("Feign request: {} {}, headers={}",
                request.method(), request.uri(), request.headers());

        return next.executeRequest(request)
                .doOnNext(response -> {
                    long elapsed = System.currentTimeMillis() - start;
                    log.info("Feign response: {} {} -> elapsed={}ms, status={}",
                            request.method(), request.uri(), elapsed, response.status());
                })
                .doOnError(error -> {
                    long elapsed = System.currentTimeMillis() - start;
                    log.error("Feign error: {} {} -> elapsed={}ms, error={}",
                            request.method(), request.uri(), elapsed, error.getMessage());
                });
    }
}
