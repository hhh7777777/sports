package com.hongyuting.sports.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
    /**
     * 页面跳转控制器
     */
@Controller
public class PageController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "user/register";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "user/dashboard";
    }

    @GetMapping("/behavior")
    public String behavior() {
        return "user/behavior";
    }

    @GetMapping("/achievements")
    public String achievements() {
        return "user/achievements";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/behaviors")
    public String adminBehaviors() {
        return "admin/behaviors";
    }

    @GetMapping("/admin/badges")
    public String adminBadges() {
        return "admin/badges";
    }

    @GetMapping("/admin/logs")
    public String adminLogs() {
        return "admin/logs";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }
    
    // 添加圣诞活动页面映射
    @GetMapping("/christmas-event")
    public String christmasEvent() {
        return "christmas-event";
    }
}