package com.examples.dubbo3.provider;

import org.apache.dubbo.config.annotation.DubboService;

import com.examples.dubbo3.api.User;
import com.examples.dubbo3.api.UserService;

/**
 * {@link UserService} 的提供者实现。
 * <p>
 * {@link DubboService @DubboService} 把本实现导出为 Triple 服务：既可被 consumer 经
 * {@code @DubboReference}（原生 Triple RPC）调用，又因接口上的 Spring Web 注解同时以
 * REST（HTTP/JSON）暴露在 Triple 端口（50052）。
 * <p>
 * 逻辑仅为演示：GET 返回桩数据，POST 原样回显并补 id。
 */
@DubboService
public class UserServiceImpl implements UserService {

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
