package com.hongyuting.sports.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.hongyuting.sports.mapper")
public class MyBatisConfig {
    // MyBatis配置
}