package com.hongyuting.sports.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    /**
     * 统一视图控制器配置
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 统一视图控制器配置
        registry.addRedirectViewController("/", "/index");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/user/login").setViewName("user/login");
        registry.addViewController("/user/register").setViewName("user/register");
        registry.addViewController("/user/dashboard").setViewName("user/dashboard");
        registry.addViewController("/user/profile").setViewName("user/profile");
        registry.addViewController("/user/behavior").setViewName("user/behavior");
        registry.addViewController("/user/achievements").setViewName("user/achievements");

        // 管理员页面路由
        registry.addViewController("/admin").setViewName("forward:/admin/dashboard");
        registry.addViewController("/admin/dashboard").setViewName("admin/dashboard");
        registry.addViewController("/admin/login").setViewName("admin/login");
        registry.addViewController("/admin/users").setViewName("admin/users");
        registry.addViewController("/admin/behaviors").setViewName("admin/behaviors");
        registry.addViewController("/admin/badges").setViewName("admin/badges");
        registry.addViewController("/admin/logs").setViewName("admin/logs");
        
        // 活动页面路由
        registry.addViewController("/newyear").setViewName("newyear");
    }
}