package com.epidemic.pandemic.feign;

import com.epidemic.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * UserFeignClient 降级工厂
 * 当 user-service 不可用时，提供降级响应
 */
@Component
@Slf4j
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("UserFeignClient 调用失败: {}", cause.getMessage());

        return new UserFeignClient() {
            @Override
            public Result<List<Map<String, Object>>> getRoleCounts() {
                log.warn("UserFeignClient.getRoleCounts 降级返回空列表");
                return Result.error(503, "用户服务暂时不可用");
            }

            @Override
            public Result<List<Long>> getUserIdsByRole(String role) {
                log.warn("UserFeignClient.getUserIdsByRole 降级返回空列表, role={}", role);
                return Result.error(503, "用户服务暂时不可用");
            }
        };
    }
}
