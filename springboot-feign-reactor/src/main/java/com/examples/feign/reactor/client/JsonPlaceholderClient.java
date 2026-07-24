package com.examples.feign.reactor.client;

import com.examples.feign.reactor.model.Post;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式 Feign 客户端，调用 JSONPlaceholder 免费 REST API（无需注册/鉴权）。
 * <p>
 * {@code @ReactiveFeignClient} 与 OpenFeign 的 {@code @FeignClient} 不同：
 * <ul>
 *   <li>方法必须返回 {@link Mono} 或 {@link Flux}，底层走 Spring WebClient（非阻塞 I/O）</li>
 *   <li>请求映射注解复用 Spring Web 的标准注解（{@code @GetMapping}、{@code @RequestParam} 等）</li>
 *   <li>{@code url} 直接指定目标，不依赖服务发现；也可以只用 {@code name} + 负载均衡</li>
 * </ul>
 */
@Component
@ReactiveFeignClient(name = "jsonplaceholder", url = "https://jsonplaceholder.typicode.com")
public interface JsonPlaceholderClient {

    /**
     * 获取全部 posts → {@link Flux}（0..N 条流式返回）。
     */
    @GetMapping("/posts")
    Flux<Post> getPosts();

    /**
     * 按 ID 获取单条 post → {@link Mono}（0..1 条）。
     */
    @GetMapping("/posts/{id}")
    Mono<Post> getPost(@PathVariable("id") Long id);
}
