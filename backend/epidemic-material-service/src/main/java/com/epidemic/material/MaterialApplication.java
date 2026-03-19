package com.epidemic.material;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.epidemic.material", "com.epidemic.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.epidemic.material.mapper")
public class MaterialApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaterialApplication.class, args);
        System.out.println("========================================");
        System.out.println("  物资服务启动成功！");
        System.out.println("  服务地址: http://localhost:8082");
        System.out.println("  API文档: http://localhost:8082/doc.html");
        System.out.println("========================================");
    }
}
