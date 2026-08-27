package com.examples.grpc.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examples.grpc.api.proto.CreateUserRequest;
import com.examples.grpc.api.proto.GetUserRequest;
import com.examples.grpc.api.proto.UserServiceGrpc;

import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * 消费者侧的 HTTP 触发器：把一次 HTTP 请求转成对 provider 的 gRPC 调用（对应 sca 的 /sca/users/**）。
 * <p>
 * {@link GrpcClient @GrpcClient("grpc-provider")} 按 client name 取 devh 管理的 {@link Channel}
 * （连接池/生命周期由 starter 负责，name 对应 yml 里 grpc.client.grpc-provider.address）。
 * 注入 Channel 后按需 new stub——stub 轻量不可变，推荐每次调用时创建。
 * OTel agent 对 gRPC client 的埋点挂在 Channel 拦截器上，newBlockingStub 出来的调用都在链路里。
 */
@RestController
@RequestMapping("/grpc/users")
public class UserConsumerController {

    @GrpcClient("grpc-provider")
    private Channel channel;

    /** GET /grpc/users/{id} → gRPC user.UserService/GetUser。 */
    @GetMapping("/{id}")
    public User getById(@PathVariable("id") Long id) {
        com.examples.grpc.api.proto.User resp = stub()
                .getUser(GetUserRequest.newBuilder().setId(id).build());
        return toDto(resp);
    }

    /** POST /grpc/users → gRPC user.UserService/CreateUser。 */
    @PostMapping
    public User create(@RequestBody User user) {
        com.examples.grpc.api.proto.User resp = stub()
                .createUser(CreateUserRequest.newBuilder()
                        .setName(user.getName())
                        .setEmail(user.getEmail())
                        .build());
        return toDto(resp);
    }

    private UserServiceGrpc.UserServiceBlockingStub stub() {
        return UserServiceGrpc.newBlockingStub(channel);
    }

    /** proto → POJO：MVC 边界映射，避免 Jackson 直接序列化 protobuf 对象。 */
    private User toDto(com.examples.grpc.api.proto.User user) {
        return new User(user.getId(), user.getName(), user.getEmail());
    }
}
