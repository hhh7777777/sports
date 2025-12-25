package com.hongyuting.sports.controller;

import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.service.BadgeService;
import com.hongyuting.sports.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
    /**
     * 页面跳转控制器
     */
@Controller
public class PageController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BadgeService badgeService;
    
    @GetMapping({"/", "/index"})
    public String home(Model model, HttpSession session) {
        // 获取当前用户信息
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("isLoggedIn", true);
                
                // 获取用户徽章信息
                List<Badge> userBadges = badgeService.getUserAchievements(userId);
                model.addAttribute("userBadges", userBadges);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
            
            // 获取公开徽章列表
            List<com.hongyuting.sports.entity.Badge> allBadges = badgeService.getAllBadges();
            model.addAttribute("allBadges", allBadges);
        }
        
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model, HttpSession session) {
        // 检查用户是否已登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            // 如果已登录，重定向到仪表板
            return "redirect:/dashboard";
        }
        
        // 可以添加注册页面需要的其他数据
        return "user/pages/register";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        // 检查用户是否已登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            // 如果已登录，重定向到仪表板
            return "redirect:/dashboard";
        }
        
        // 可以添加登录页面需要的其他数据
        return "user/pages/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // 获取当前用户信息
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                model.addAttribute("currentUser", user);
                
                // 获取用户个人统计信息
                Map<String, Object> personalStats = userService.getUserPersonalStats(userId);
                if (personalStats != null) {
                    model.addAttribute("weeklyDuration", personalStats.getOrDefault("weeklyDuration", 0));
                    model.addAttribute("weeklyCount", personalStats.getOrDefault("weeklyCount", 0));
                    model.addAttribute("monthlyDuration", personalStats.getOrDefault("monthlyDuration", 0));
                    model.addAttribute("badgeCount", personalStats.getOrDefault("badgeCount", 0));
                    model.addAttribute("myTotalDuration", personalStats.getOrDefault("totalDuration", 0));
                    model.addAttribute("myTotalRecords", personalStats.getOrDefault("totalRecords", 0));
                }
                
                // 获取用户排名信息
                Map<String, Object> rankInfo = userService.getUserRankInfo(userId);
                if (rankInfo != null) {
                    model.addAttribute("myRank", rankInfo.getOrDefault("rank", "--"));
                    model.addAttribute("totalUserCount", rankInfo.getOrDefault("totalUsers", "--"));
                }
                
                // 获取最近活动（这里可以调用行为服务获取最近活动）
                // 由于没有直接获取最近活动的方法，暂时设置为空列表
                model.addAttribute("recentActivities", new ArrayList<>());
            }
        }
        
        return "user/pages/dashboard";
    }

    @GetMapping("/behavior")
    public String behavior(Model model, HttpSession session) {
        // 获取当前用户信息
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                model.addAttribute("currentUser", user);
                
                // 这里可以添加运动记录相关数据
                // 由于没有现成的服务方法，暂时添加空数据
                model.addAttribute("behaviors", new ArrayList<>());
                model.addAttribute("behaviorTypes", new ArrayList<>());
            }
        }
        
        return "user/pages/behavior";
    }

    @GetMapping("/achievements")
    public String achievements(Model model, HttpSession session) {
        // 获取当前用户信息
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("allBadges", new ArrayList<>());
                model.addAttribute("earnedBadges", new ArrayList<>());
                model.addAttribute("lockedBadges", new ArrayList<>());
                model.addAttribute("totalBadges", 0);
                model.addAttribute("earnedBadgesCount", 0);
            }
        }
        
        return "user/pages/achievements";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        // 获取当前用户信息
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                model.addAttribute("currentUser", user);
            }
        }
        
        return "user/pages/profile";
    }


    @GetMapping("/admin/login")
    public String adminLogin() {
            return "admin/pages/login";
        }



    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/pages/users";
    }

    @GetMapping("/admin/behaviors")
    public String adminBehaviors() {
        return "admin/pages/behaviors";
    }



    @GetMapping("/admin/logs")
    public String adminLogs() {
        return "admin/pages/logs";
    }


    
    // 添加活动页面映射
    @GetMapping("/newyear")
    public String christmasEvent(Model model, HttpSession session) {
        // 获取当前用户信息
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("isLoggedIn", true);
                
                // 这里可以添加新年活动相关数据
                // 由于没有现成的服务方法，暂时添加空数据
                model.addAttribute("activities", new ArrayList<>());
                model.addAttribute("badges", new ArrayList<>());
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        
        return "newyear";
    }
}