package com.examples.feign.reactor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 与 JSONPlaceholder {@code /posts} 返回结构对应的 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Long id;
    private Long userId;
    private String title;
    private String body;
}
