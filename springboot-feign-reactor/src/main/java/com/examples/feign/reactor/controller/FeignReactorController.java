package com.examples.feign.reactor.controller;

import com.examples.feign.reactor.client.JsonPlaceholderClient;
import com.examples.feign.reactor.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 透传控制器：HTTP 请求 → {@link JsonPlaceholderClient}（WebClient）→ 返回 {@link Mono}/{@link Flux}。
 * <p>
 * Spring MVC 2.7 原生支持响应式返回值（{@link Mono}/{@link Flux}），无需引入 spring-webflux。
 */
@RestController
@RequiredArgsConstructor
public class FeignReactorController {

    private final JsonPlaceholderClient client;

    /**
     * <pre>curl http://localhost:8083/posts</pre>
     */
    @GetMapping("/posts")
    public Flux<Post> getPosts() {
        return client.getPosts();
    }

    /**
     * <pre>curl http://localhost:8083/posts/1</pre>
     */
    @GetMapping("/posts/{id}")
    public Mono<Post> getPost(@PathVariable Long id) {
        return client.getPost(id);
    }
}
