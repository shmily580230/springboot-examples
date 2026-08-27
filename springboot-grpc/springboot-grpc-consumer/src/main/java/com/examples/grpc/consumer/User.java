package com.examples.grpc.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * consumer 侧的 HTTP 响应模型。
 * <p>
 * 不直接把 protobuf 的 {@code com.examples.grpc.api.proto.User} 当 JSON 返回：
 * Jackson 会把它所有 getter 都序列化（getSerializedSize/getUnknownFields 等），出来的 JSON 不可用，
 * 故在 MVC 边界做一次 proto → POJO 映射（与 sca-api 的 User 等价）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String name;

    private String email;
}
