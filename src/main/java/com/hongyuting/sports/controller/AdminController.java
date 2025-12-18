package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.service.AdminService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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