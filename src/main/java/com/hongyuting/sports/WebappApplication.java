package com.hongyuting.sports;

import org.springframework.boot.SpringApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.hongyuting.sports.mapper")
@EnableTransactionManagement
@EnableCaching
@EnableScheduling
public class WebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
        System.out.println("=========================================");
        System.out.println("运动管理平台启动成功!");
        System.out.println("本地访问: http://localhost:8080/index");
        System.out.println("=========================================");
    }
}
