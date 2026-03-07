package com.epidemic.gateway.config;

import com.epidemic.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动预热
 * 在启动完成后预热关键组件，避免首次请求延迟
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WarmupRunner implements ApplicationRunner {

    private final JwtUtil jwtUtil;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始预热关键组件...");

        // 预热JWT：触发一次完整的Token生成和解析流程
        try {
            String warmupToken = jwtUtil.generateToken(0L, "_warmup_", "system");
            jwtUtil.getClaimsFromToken(warmupToken);
            log.info("JWT组件预热完成");
        } catch (Exception e) {
            log.warn("JWT预热失败: {}", e.getMessage());
        }

        log.info("关键组件预热完成");
    }
}
