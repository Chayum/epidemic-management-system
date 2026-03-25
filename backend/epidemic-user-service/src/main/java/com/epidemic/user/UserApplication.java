package com.epidemic.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.epidemic.user", "com.epidemic.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.epidemic.common.feign"})
@EnableRabbit
@EnableAsync
@MapperScan({"com.epidemic.user.mapper", "com.epidemic.common.mapper"})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("========================================");
        System.out.println("  用户服务启动成功！");
        System.out.println("  服务地址: http://localhost:8081");
        System.out.println("  API文档: http://localhost:8081/doc.html");
        System.out.println("========================================");
    }
}
