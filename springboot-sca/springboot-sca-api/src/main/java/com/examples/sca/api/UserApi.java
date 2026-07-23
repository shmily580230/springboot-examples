package com.examples.sca.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务契约（Spring Cloud OpenFeign 共享接口模式）。
 * <p>
 * 接口只带 Spring Web 注解（{@code @GetMapping}/{@code @PostMapping}），<b>不带</b> {@code @FeignClient}——这样：
 * <ul>
 *   <li>provider：{@code @RestController class UserController implements UserApi}，自动继承这些映射；</li>
 *   <li>consumer：{@code @FeignClient(name="sca-provider") interface UserFeignClient extends UserApi}，
 *       复用同一组路径映射，Feign 按 service name 经 Nacos 负载均衡发起 HTTP 调用。</li>
 * </ul>
 * 契约与 {@link User} 模型放在共享 api 模块，provider/consumer 共同依赖，避免重复定义。
 */
public interface UserApi {

    /** 按 id 查询用户：GET /users/{id}。 */
    @GetMapping("/users/{id}")
    User getById(@PathVariable("id") Long id);

    /** 创建用户：POST /users（JSON body），原样回显并补 id。 */
    @PostMapping("/users")
    User create(@RequestBody User user);
}
