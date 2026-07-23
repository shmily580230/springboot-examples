package com.examples.sca.api;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户模型。provider / consumer 共享（经 api jar）。
 * <p>
 * 本模块走 HTTP/JSON（Feign），本不需要 {@link Serializable}；但保留它，
 * 便于将来切换到 Dubbo 等需二进制序列化的协议。
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
