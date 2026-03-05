package com.epidemic.material.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库初始化器
 * 用于执行新增的库存管理相关表的DDL脚本
 */
@Component
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and initializing inventory subsystem tables...");
        try (Connection connection = dataSource.getConnection()) {
            Resource resource = new ClassPathResource("inventory_schema.sql");
            if (resource.exists()) {
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("Inventory subsystem tables initialized successfully.");
            } else {
                log.warn("inventory_schema.sql not found.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize inventory tables: " + e.getMessage(), e);
        }
    }
}
