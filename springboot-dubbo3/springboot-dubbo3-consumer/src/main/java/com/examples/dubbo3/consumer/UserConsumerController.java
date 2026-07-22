package com.examples.dubbo3.consumer;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examples.dubbo3.api.User;
import com.examples.dubbo3.api.UserService;

/**
 * 消费者侧的 REST 触发器：把一次 HTTP 请求转成对 provider 的「原生 Triple RPC」调用。
 * <p>
 * 路径刻意写成 {@code /dubbo/users/**}（而非 {@code /users/**}），避免与 Triple 自身的
 * REST 映射混淆——这里走的是 Spring MVC → @DubboReference 的 Triple 二进制 RPC，
 * 与 provider 直接对外暴露的 REST（curl :50052/users/...）是两条不同链路。
 * <p>
 * {@link DubboReference @DubboReference} 经 Nacos 发现 provider；provider/consumer 分进程，
 * 本就是跨进程真实远程调用，无需 scope=remote。{@code check=false} 让 provider 未就绪时
 * consumer 仍能启动（演示时仍建议先起 provider）。
 */
@RestController
@RequestMapping("/dubbo/users")
public class UserConsumerController {

    @DubboReference(check = false)
    private UserService userService;

    /** GET /dubbo/users/{id} → 原生 Triple RPC 调 provider.getById。 */
    @GetMapping("/{id}")
    public User getById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    /** POST /dubbo/users → 原生 Triple RPC 调 provider.create。 */
    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }
}
