package com.examples.sca.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examples.sca.api.User;
import com.examples.sca.api.UserApi;

/**
 * 消费者侧的 HTTP 触发器：把一次 HTTP 请求转成对 provider 的 Feign（HTTP）调用。
 * <p>
 * 路径刻意写成 {@code /sca/users/**}（而非 {@code /users/**}），避免与 Feign 契约路径混淆，
 * 也便于在 trace 里区分 consumer 入口 span 与 provider 处理 span。
 */
@RestController
@RequestMapping("/sca/users")
public class UserConsumerController {

    private final UserApi userApi;

    public UserConsumerController(UserFeignClient userFeignClient) {
        this.userApi = userFeignClient;
    }

    /** GET /sca/users/{id} → Feign HTTP 调 provider.getById。 */
    @GetMapping("/{id}")
    public User getById(@PathVariable("id") Long id) {
        return userApi.getById(id);
    }

    /** POST /sca/users → Feign HTTP 调 provider.create。 */
    @PostMapping
    public User create(@RequestBody User user) {
        return userApi.create(user);
    }
}
