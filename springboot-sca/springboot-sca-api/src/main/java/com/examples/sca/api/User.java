package com.examples.sca.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户模型。provider / consumer 共享（经 api jar）。
 * <p>
 * 用 lombok 生成 getter/setter 与全参/无参构造器。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String name;

    private String email;
}
