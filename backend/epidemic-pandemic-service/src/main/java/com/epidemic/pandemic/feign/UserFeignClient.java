package com.epidemic.pandemic.feign;

import com.epidemic.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign Client
 * 用于调用 user-service 的用户接口
 */
@FeignClient(name = "epidemic-user-service", path = "/user",
             fallbackFactory = UserFeignClientFallbackFactory.class)
public interface UserFeignClient {

    /**
     * 获取用户角色统计
     * @return 各角色用户数量
     */
    @GetMapping("/stats/role-counts")
    Result<List<Map<String, Object>>> getRoleCounts();

    /**
     * 根据角色获取用户ID列表
     * @param role 角色（可选）
     * @return 用户ID列表
     */
    @GetMapping("/ids/by-role")
    Result<List<Long>> getUserIdsByRole(@RequestParam(required = false) String role);
}