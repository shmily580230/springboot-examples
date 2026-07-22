package com.examples.dubbo3.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务接口。
 * <p>
 * Dubbo 3.3 起，{@code tri}（Triple）协议原生支持 REST：在服务接口上加 Spring Web 注解，
 * 同一个服务就会「双发」——既是 Triple 二进制 RPC（provider 侧 @DubboService 导出、
 * consumer 侧 @DubboReference 调用），又对外暴露 REST（HTTP/JSON），可直接用 curl 访问。
 * <p>
 * <b>路径写在方法上、且必须是完整路径（不依赖类级 @RequestMapping）：</b>实测 Triple REST 在
 * 「类级 @RequestMapping("/users") + 方法空路径」时会把空路径解析成<b>方法名</b>（如 create → /users/create），
 * 导致 POST /users（基路径）404。因此这里每个方法的 @GetMapping/@PostMapping 都显式写出完整路径，
 * 拿到标准 REST 形态：GET /users/{id}、POST /users。
 * <p>
 * 接口与 {@link User} 模型放在共享 api 模块，provider / consumer 共同依赖，避免重复定义。
 */
public interface UserService {

    /** 按 id 查询用户：GET /users/{id}（REST 路径参数）。 */
    @GetMapping("/users/{id}")
    User getById(@PathVariable("id") Long id);

    /** 创建用户：POST /users（REST JSON body），原样回显并补 id。 */
    @PostMapping("/users")
    User create(@RequestBody User user);
}
