package com.hongyuting.sports.config;

import com.hongyuting.sports.interceptor.AuthInterceptor;
import com.hongyuting.sports.interceptor.LogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 认证拦截器
     */
    @Autowired
    private AuthInterceptor authInterceptor;
    /**
     * 日志拦截器
     */
    @Autowired
    private LogInterceptor logInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器（保护需要登录的接口）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                // 保护所有/api接口
                .excludePathPatterns(
                        // 排除不需要认证的接口
                        "/api/user/login",
                        "/api/user/register",
                        "/api/common/captcha",
                        "/api/common/server-time",
                        "/api/test",
                        // 排除管理员登录相关接口
                        "/api/admin/login",
                        "/api/admin/refresh",
                        // 排除静态资源
                        "/css/**", "/js/**", "/images/**",
                        "/static/**", "/uploads/**"
                );
                
        // 日志拦截器
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/common/captcha",
                        "/css/**", "/js/**", "/images/**"
                );
    }
    /**
     * 添加静态资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}