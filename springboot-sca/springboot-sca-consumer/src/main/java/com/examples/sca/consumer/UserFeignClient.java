package com.examples.sca.consumer;

import org.springframework.cloud.openfeign.FeignClient;

import com.examples.sca.api.UserApi;

/**
 * Feign 客户端：复用 {@link UserApi} 的路径映射，按 service name {@code sca-provider} 寻址。
 * <p>
 * 不在 api 模块上加 @FeignClient，是为了让 api 保持「纯契约」、provider（@RestController）也能复用它。
 * {@code @FeignClient} 留在 consumer 侧，由 {@code @EnableFeignClients} 扫描注册。
 */
@FeignClient(name = "sca-provider")
public interface UserFeignClient extends UserApi {
}
