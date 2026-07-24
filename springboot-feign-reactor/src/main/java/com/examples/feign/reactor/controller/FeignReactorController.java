package com.examples.feign.reactor.controller;

import com.examples.feign.reactor.client.JsonPlaceholderClient;
import com.examples.feign.reactor.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 透传控制器：HTTP 请求 → {@link JsonPlaceholderClient}（WebClient）→ 返回 {@link List}/{@link Post}。
 */
@RestController
@RequiredArgsConstructor
public class FeignReactorController {

    private final JsonPlaceholderClient client;

    /**
     * <pre>curl http://localhost:8083/posts</pre>
     */
    @GetMapping("/posts")
    public List<Post> getPosts() {
        return client.getPosts().collectList().block();
    }

    /**
     * <pre>curl http://localhost:8083/posts/1</pre>
     */
    @GetMapping("/posts/{id}")
    public Post getPost(@PathVariable Long id) {
        return client.getPost(id).block();
    }
}
