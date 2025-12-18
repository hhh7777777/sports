package com.hongyuting.sports.service;

import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.dto.ResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {

    ResponseDTO addAdminLog(AdminLog adminLog);

    List<AdminLog> getAdminLogs(Integer adminId, String operation, LocalDateTime startTime, LocalDateTime endTime);

    List<AdminLog> getAdminLogsByTarget(String targetType, Integer targetId);

    ResponseDTO clearOldLogs(LocalDateTime beforeTime);

    Integer getAdminOperationCount(Integer adminId, LocalDateTime startTime, LocalDateTime endTime);

    // 新增方法：获取系统统计信息
    Map<String, Object> getSystemStats();

    // 新增方法：获取所有用户列表（用于管理页面）
    List<Map<String, Object>> getAllUsers();

    // 新增方法：获取所有行为记录
    List<Map<String, Object>> getAllBehaviors();

    // 新增方法：获取所有徽章
    List<Map<String, Object>> getAllBadges();

    // 新增方法：获取系统日志
    List<Map<String, Object>> getSystemLogs();
}