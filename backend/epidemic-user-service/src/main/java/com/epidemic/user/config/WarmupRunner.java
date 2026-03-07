package com.epidemic.user.config;

import com.epidemic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 应用启动预热
 * 在启动完成后预热关键组件，避免首次请求延迟
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WarmupRunner implements ApplicationRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始预热关键组件...");

        // 预热BCryptPasswordEncoder（首次encode非常慢）
        try {
            passwordEncoder.encode("warmup");
            log.info("BCryptPasswordEncoder预热完成");
        } catch (Exception e) {
            log.warn("BCrypt预热失败: {}", e.getMessage());
        }

        // 预热数据库连接池（触发HikariCP建立连接）
        try {
            userService.count();
            log.info("数据库连接池预热完成");
        } catch (Exception e) {
            log.warn("数据库预热失败: {}", e.getMessage());
        }

        log.info("关键组件预热完成");
    }
}
