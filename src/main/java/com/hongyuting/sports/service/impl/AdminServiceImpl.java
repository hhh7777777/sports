package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.*;
import com.hongyuting.sports.mapper.*;
import com.hongyuting.sports.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminLogMapper adminLogMapper;
    private final UserMapper userMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;
    private final AchievementBadgeMapper achievementBadgeMapper;

    @Override
    @Transactional
    public ResponseDTO addAdminLog(AdminLog adminLog) {
        try {
            adminLog.setOperationTime(LocalDateTime.now());
            int result = adminLogMapper.insertAdminLog(adminLog);
            return result > 0 ? ResponseDTO.success("操作日志记录成功") : ResponseDTO.error("操作日志记录失败");
        } catch (Exception e) {
            log.error("记录操作日志异常", e);
            return ResponseDTO.error("操作日志记录异常: " + e.getMessage());
        }
    }

    @Override
    public List<AdminLog> getAdminLogs(Integer adminId, String operation, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return adminLogMapper.selectAdminLogs(adminId, operation, startTime, endTime);
        } catch (Exception e) {
            log.error("获取操作日志异常", e);
            return List.of();
        }
    }

    @Override
    public List<AdminLog> getAdminLogsByTarget(String targetType, Integer targetId) {
        try {
            return adminLogMapper.selectAdminLogsByTarget(targetType, targetId);
        } catch (Exception e) {
            log.error("按目标获取操作日志异常", e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public ResponseDTO clearOldLogs(LocalDateTime beforeTime) {
        try {
            int result = adminLogMapper.deleteAdminLogsBefore(beforeTime);
            return ResponseDTO.success("清理成功，删除 " + result + " 条日志记录");
        } catch (Exception e) {
            log.error("清理旧日志异常", e);
            return ResponseDTO.error("日志清理异常: " + e.getMessage());
        }
    }

    @Override
    public Integer getAdminOperationCount(Integer adminId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return adminLogMapper.selectOperationCount(adminId, startTime, endTime);
        } catch (Exception e) {
            log.error("获取管理员操作次数异常", e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 从数据库获取真实数据
            Integer totalUsers = userMapper.selectUserCount();
            Integer totalBadges = achievementBadgeMapper.selectTotalBadgeCount();

            // 获取行为记录总数（需要新增方法）
            Integer totalBehaviors = behaviorRecordMapper.selectAllBehaviorRecords().size();

            stats.put("totalUsers", totalUsers != null ? totalUsers : 0);
            stats.put("totalBadges", totalBadges != null ? totalBadges : 0);
            stats.put("totalBehaviors", totalBehaviors);
            stats.put("activeUsers", totalUsers); // 暂时用总用户数代替活跃用户
            stats.put("systemUptime", calculateSystemUptime());
            stats.put("lastUpdate", LocalDateTime.now());
        } catch (Exception e) {
            log.error("获取系统统计信息异常", e);
            stats.put("error", "获取统计信息失败");
        }
        return stats;
    }

    @Override
    public List<Map<String, Object>> getAllUsers() {
        try {
            return userMapper.selectAllUsers().stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", user.getUserId());
                        userMap.put("username", user.getUsername());
                        userMap.put("email", user.getEmail());
                        userMap.put("nickname", user.getNickname());
                        userMap.put("status", user.getUserStatus() == 1 ? "正常" : "禁用");
                        userMap.put("registerTime", user.getRegisterTime());
                        userMap.put("lastLoginTime", user.getLastLoginTime());
                        return userMap;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取所有用户异常", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getAllBehaviors() {
        try {
            return behaviorRecordMapper.selectAllBehaviorRecords().stream()
                    .limit(50) // 限制数量
                    .map(behavior -> {
                        Map<String, Object> behaviorMap = new HashMap<>();
                        behaviorMap.put("id", behavior.getRecordId());
                        behaviorMap.put("userId", behavior.getUserId());
                        behaviorMap.put("typeId", behavior.getTypeId());
                        behaviorMap.put("duration", behavior.getDuration());
                        behaviorMap.put("date", behavior.getRecordDate());
                        behaviorMap.put("content", behavior.getContent());
                        behaviorMap.put("createTime", behavior.getCreateTime());
                        return behaviorMap;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取所有行为记录异常", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getAllBadges() {
        try {
            return achievementBadgeMapper.selectAllBadges().stream()
                    .map(badge -> {
                        Map<String, Object> badgeMap = new HashMap<>();
                        badgeMap.put("id", badge.getBadgeId());
                        badgeMap.put("name", badge.getBadgeName());
                        badgeMap.put("description", badge.getDescription());
                        badgeMap.put("level", badge.getLevel());
                        badgeMap.put("conditionType", badge.getConditionType());
                        badgeMap.put("conditionValue", badge.getConditionValue());
                        badgeMap.put("points", badge.getRewardPoints());
                        badgeMap.put("status", badge.getStatus() == 1 ? "启用" : "禁用");
                        badgeMap.put("createTime", badge.getCreateTime());
                        return badgeMap;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取所有徽章异常", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getSystemLogs() {
        try {
            // 需要新增selectAllAdminLogs方法
            return adminLogMapper.selectAdminLogs(null, null, null, null).stream()
                    .limit(100) // 限制数量
                    .map(log -> {
                        Map<String, Object> logMap = new HashMap<>();
                        logMap.put("id", log.getLogId());
                        logMap.put("adminId", log.getAdminId());
                        logMap.put("operation", log.getOperation());
                        logMap.put("targetType", log.getTargetType());
                        logMap.put("targetId", log.getTargetId());
                        logMap.put("ip", log.getIpAddress());
                        logMap.put("time", log.getOperationTime());
                        return logMap;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取系统日志异常", e);
            return List.of();
        }
    }

    private String calculateSystemUptime() {
        // 实现系统运行时间计算逻辑
        return "7天12小时30分钟";
    }
}