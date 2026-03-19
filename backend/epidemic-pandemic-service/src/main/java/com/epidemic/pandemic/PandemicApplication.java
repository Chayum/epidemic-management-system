package com.epidemic.pandemic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.epidemic.pandemic", "com.epidemic.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.epidemic.pandemic.mapper")
public class PandemicApplication {

    public static void main(String[] args) {
        SpringApplication.run(PandemicApplication.class, args);
        System.out.println("========================================");
        System.out.println("  疫情信息服务启动成功！");
        System.out.println("  服务地址: http://localhost:8083");
        System.out.println("  API文档: http://localhost:8083/doc.html");
        System.out.println("========================================");
    }
}
