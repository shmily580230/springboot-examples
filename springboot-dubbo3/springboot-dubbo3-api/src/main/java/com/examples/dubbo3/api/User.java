package com.examples.dubbo3.api;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户模型。provider / consumer 共享（经 api jar）。
 * <p>
 * <b>必须实现 {@link Serializable}</b>：REST 路径走 JSON（fastjson2）无需它，但「原生 Triple RPC」
 * 路径走 hessian2，且 Dubbo 3.3 默认开启序列化安全严格检查（STRICT）——非 Serializable 的类会被
 * 拒绝序列化（报 Serialized class ... has not implement Serializable）。故 RPC 往返的 DTO 都要实现它。
 * <p>
 * 用 lombok 生成 getter/setter 与全参/无参构造器。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String email;
}
