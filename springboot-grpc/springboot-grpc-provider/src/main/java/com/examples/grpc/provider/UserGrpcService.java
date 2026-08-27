package com.examples.grpc.provider;

import com.examples.grpc.api.proto.CreateUserRequest;
import com.examples.grpc.api.proto.GetUserRequest;
import com.examples.grpc.api.proto.User;
import com.examples.grpc.api.proto.UserServiceGrpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * {@code user.UserService} 的提供者实现：继承 codegen 出的 {@link UserServiceGrpc.UserServiceImplBase}，
 * 标 {@link GrpcService @GrpcService} 即被 devh starter 注册进 gRPC server（等价于 Spring MVC 的 @RestController）。
 * <p>
 * 与 HTTP/JSON 的 {@code @RestController} 有两点形态差异：
 * <ul>
 *   <li>响应不 return，而是往 {@code StreamObserver} 里 onNext/onCompleted（onError 报错）；</li>
 *   <li>消息体是 protobuf 强类型对象（builder 构建），没有手写序列化。</li>
 * </ul>
 * 逻辑与 sca 的 UserController 相同：GET 返回桩数据，Create 原样回显并补 id。
 */
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    /** user.UserService/GetUser：按 id 查用户。 */
    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        User user = User.newBuilder()
                .setId(request.getId())
                .setName("user-" + request.getId())
                .setEmail("user" + request.getId() + "@example.com")
                .build();
        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }

    /** user.UserService/CreateUser：补 id 后原样回显。 */
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        User user = User.newBuilder()
                .setId(System.currentTimeMillis())
                .setName(request.getName())
                .setEmail(request.getEmail())
                .build();
        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }
}
