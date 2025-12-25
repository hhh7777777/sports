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
        return "user/pages/register";
    }

    @GetMapping("/login")
    public String login() {
        return "user/pages/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "user/pages/dashboard";
    }

    @GetMapping("/behavior")
    public String behavior() {
        return "user/pages/behavior";
    }

    @GetMapping("/achievements")
    public String achievements() {
        return "user/pages/achievements";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/pages/profile";
    }


    @GetMapping("/admin/login")
    public String adminLogin() {
            return "admin/pages/login";
        }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/pages/dashboard";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/pages/users";
    }

    @GetMapping("/admin/behaviors")
    public String adminBehaviors() {
        return "admin/pages/behaviors";
    }

    @GetMapping("/admin/badges")
    public String adminBadges() {
        return "admin/pages/badges";
    }

    @GetMapping("/admin/logs")
    public String adminLogs() {
        return "admin/pages/logs";
    }


    
    // 添加活动页面映射
    @GetMapping("/newyear")
    public String christmasEvent() {
        return "newyear";
    }
}