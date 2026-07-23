package com.examples.sca.provider;

import org.springframework.web.bind.annotation.RestController;

import com.examples.sca.api.User;
import com.examples.sca.api.UserApi;

/**
 * {@link UserApi} 的提供者实现：普通 Spring MVC {@code @RestController}。
 * <p>
 * 经 Nacos 注册为 {@code sca-provider}；consumer 的 @FeignClient 按此 name 发起 HTTP 调用，
 * 命中这里的 {@code /users/**} 端点。逻辑仅为演示：GET 返回桩数据，POST 原样回显并补 id。
 */
@RestController
public class UserController implements UserApi {

    @Override
    public User getById(Long id) {
        return new User(id, "user-" + id, "user" + id + "@example.com");
    }

    @Override
    public User create(User user) {
        if (user.getId() == null) {
            user.setId(System.currentTimeMillis());
        }
        return user;
    }
}
