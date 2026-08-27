// 共享 API：.proto 契约（唯一事实源）→ 生成 message + grpc-java stub，供 provider / consumer 复用。
// 本模块是纯库，不是 Spring Boot 应用：根项目对所有子模块套了 spring-boot 插件，api 无主类，
// 若不禁用 bootJar，构建时会因找不到主类而报错（与 sca-api 同理）。
//
// 版本搭配（关键，均为 JDK8 兼容线）：
//   protobuf 3.24.x  ↔ protoc 3.24.4（codegen 与 runtime 必须同版本线）
//   grpc-java 1.58.0 ↔ devh grpc-spring-boot-starter 2.15.0.RELEASE 内置的 grpc-bom:1.58.0
import com.google.protobuf.gradle.id

plugins {
    // java-library：生成 stub 的公开类型直接引用 grpc/protobuf 类，api 依赖配置让 consumer 编译期确定性拿到它们
    `java-library`
    id("com.google.protobuf") version "0.9.5"
}

val protobufVersion = "3.24.4"
val grpcVersion = "1.58.0"

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    // 生成的代码编译所需的 grpc 运行时（provider/consumer 传递依赖即可用，声明为 api 语义上更准确：
    // 本模块的公开类型（User/UserServiceGrpc）直接引用了这些包）
    add("api", "io.grpc:grpc-protobuf:$grpcVersion")
    add("api", "io.grpc:grpc-stub:$grpcVersion")
    // grpc 1.58 生成的 stub 用自带 @io.grpc.stub.annotations.GrpcGenerated，JDK8 下无需 javax.annotation；
    // 此依赖仅是 JDK11+ 编译的保险（devh 官方文档建议）
    add("compileOnly", "org.apache.tomcat:annotations-api:6.0.53")
    // protobuf-java 运行时：显式钉到与 protoc 一致的 3.24.4（grpc 1.58 传递的是 3.24.0，靠 Gradle 取高者对齐）
    add("api", "com.google.protobuf:protobuf-java:$protobufVersion")
}

// protobuf codegen：protoc 生成 message，protoc-gen-grpc-java 生成 RPC stub。
// 两个二进制按当前平台（osx-aarch_64 等）自动从 maven 仓库下载，产出挂在 build/generated/source/proto/ 下。
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        // 声明 grpc codegen 插件（protoc 的插件，非 Gradle 插件）
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").configureEach {
            plugins {
                // 对 src/main/proto 里所有 .proto 启用 grpc stub 生成
                id("grpc")
            }
        }
    }
}
