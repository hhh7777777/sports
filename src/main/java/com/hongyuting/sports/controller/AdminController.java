package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.AdminLoginDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.service.AdminService;
import com.hongyuting.sports.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    /**
     * 管理员服务
     */
    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseDTO login(@RequestBody AdminLoginDTO loginDTO, HttpServletRequest request) {
        String clientIP = getClientIP(request);
        return adminService.login(loginDTO, clientIP);
    }

    /**
     * 管理员退出登录
     */
    @PostMapping("/logout")
    public ResponseDTO logout(@RequestAttribute Integer adminId,
                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        return adminService.logout(adminId, token);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseDTO refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = authHeader.substring(7); // 去掉"Bearer "前缀
        return adminService.refreshAccessToken(refreshToken);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseDTO changePassword(@RequestAttribute Integer adminId,
                                      @RequestBody ChangePasswordRequest request) {
        return adminService.changePassword(adminId, request.getOldPassword(), request.getNewPassword());
    }

    /**
     * 验证Token有效性
     */
    @GetMapping("/validate")
    public ResponseDTO validateToken(@RequestAttribute Integer adminId,
                                     @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        
        // 验证JWT Token格式是否有效
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("认证令牌格式无效");
        }

        // 验证Token是否存在于Redis中
        if (adminService.existsToken(token)) {
            return ResponseDTO.error("认证令牌无效或已过期");
        }

        // 获取管理员信息
        Admin admin = adminService.getAdminById(adminId);
        if (admin != null) {
            admin.setPassword(null);
            admin.setSalt(null);
        }

        return ResponseDTO.success("Token有效", admin);
    }

    /**
     * 创建管理员（需要超级管理员权限）
     */
    @PostMapping("/create")
    public ResponseDTO createAdmin(@RequestBody com.hongyuting.sports.entity.Admin admin,
                                   @RequestAttribute Integer adminId) {
        // 这里可以添加权限验证逻辑
        return adminService.createAdmin(admin);
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
    /**
     * 获取操作日志
     */
    @GetMapping("/logs")
    public ResponseDTO getAdminLogs(
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        List<AdminLog> logs = adminService.getAdminLogs(adminId, operation, startTime, endTime);
        return ResponseDTO.success("获取成功", logs);
    }

    /**
     * 按目标获取操作日志
     */
    @GetMapping("/logs/target")
    public ResponseDTO getAdminLogsByTarget(@RequestParam String targetType,
                                            @RequestParam(required = false) Integer targetId) {
        List<AdminLog> logs = adminService.getAdminLogsByTarget(targetType, targetId);
        return ResponseDTO.success("获取成功", logs);
    }

    /**
     * 清理旧日志
     */
    @DeleteMapping("/logs/clean")
    public ResponseDTO clearOldLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        return adminService.clearOldLogs(beforeTime);
    }

    /**
     * 获取管理员操作统计
     */
    @GetMapping("/stats/operation-count")
    public ResponseDTO getAdminOperationCount(
            @RequestParam Integer adminId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30); // 默认最近30天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Integer count = adminService.getAdminOperationCount(adminId, startTime, endTime);
        return ResponseDTO.success("获取成功", count);
    }

    /**
     * 添加操作日志（通常由拦截器自动调用）
     */
    @PostMapping("/log")
    public ResponseDTO addAdminLog(@RequestBody AdminLog adminLog,
                                   @RequestAttribute Integer adminId) {
        adminLog.setAdminId(adminId);
        return adminService.addAdminLog(adminLog);
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats/system")
    public ResponseDTO getSystemStats() {
        // 这里可以返回用户总数、活跃用户数、总记录数等统计信息
        // 需要扩展Service来实现具体逻辑
        return ResponseDTO.success("获取成功", "系统统计信息");
    }
}