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
        registry.addViewController("/user/login").setViewName("user/pages/login");
        registry.addViewController("/user/register").setViewName("user/pages/register");
        registry.addViewController("/user/dashboard").setViewName("user/pages/dashboard");
        registry.addViewController("/user/profile").setViewName("user/pages/profile");
        registry.addViewController("/user/behavior").setViewName("user/pages/behavior");
        registry.addViewController("/user/achievements").setViewName("user/pages/achievements");

        // 管理员页面路由
        registry.addViewController("/admin").setViewName("forward:/admin/pages/dashboard");
        registry.addViewController("/admin/dashboard").setViewName("admin/pages/dashboard");
        registry.addViewController("/admin/login").setViewName("admin/pages/login");
        registry.addViewController("/admin/users").setViewName("admin/pages/users");
        registry.addViewController("/admin/behaviors").setViewName("admin/pages/behaviors");
        registry.addViewController("/admin/badges").setViewName("admin/pages/badges");
        registry.addViewController("/admin/logs").setViewName("admin/pages/logs");
        
        // 活动页面路由
        registry.addViewController("/newyear").setViewName("newyear");
    }
}