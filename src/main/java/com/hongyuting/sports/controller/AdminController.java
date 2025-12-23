package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.AdminLoginDTO;
import com.hongyuting.sports.dto.BadgeDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.service.AdminService;
import com.hongyuting.sports.service.BadgeService;
import com.hongyuting.sports.service.BehaviorService;
import com.hongyuting.sports.service.FileUploadService;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    /**
     * 管理员服务
     */
    private final AdminService adminService;
    private final BehaviorService behaviorService;
    private final BadgeService badgeService;
    private final UserService userService;
    private final FileUploadService fileUploadService;
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
        if (!adminService.existsToken(token)) {
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
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        // 获取符合条件的所有日志
        List<AdminLog> allLogs = adminService.getAdminLogs(adminId, operation, startTime, endTime);
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        // 实现分页逻辑
        int totalCount = allLogs != null ? allLogs.size() : 0;
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        List<AdminLog> pagedLogs;
        if (totalCount > 0) {
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalCount);
            // 确保索引有效
            fromIndex = Math.max(0, Math.min(fromIndex, totalCount));
            toIndex = Math.max(fromIndex, Math.min(toIndex, totalCount));
            pagedLogs = allLogs.subList(fromIndex, toIndex);
        } else {
            pagedLogs = new ArrayList<>();
        }
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("logs", pagedLogs);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);
        result.put("pageSize", size);
        
        return ResponseDTO.success("获取成功", result);
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
        try {
            // 获取用户总数
            List<User> allUsers = userService.getAllUsers();
            int totalUsers = allUsers != null ? allUsers.size() : 0;

            // 获取今日活跃用户数（今天有行为记录的用户数）
            LocalDate today = LocalDate.now();
            List<Behavior> todaysBehaviors = behaviorService.getBehaviorRecordsByDate(today, today);
            int activeToday = todaysBehaviors != null ? (int) todaysBehaviors.stream()
                    .map(Behavior::getUserId)
                    .distinct()
                    .count() : 0;

            // 获取行为记录总数
            int totalRecords = behaviorService.getTotalBehaviorRecords();

            // 获取徽章总数
            int totalBadges = badgeService.getTotalBadgeCount();

            // 构造返回结果
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("activeToday", activeToday);
            stats.put("totalRecords", totalRecords);
            stats.put("totalBadges", totalBadges);

            return ResponseDTO.success("获取成功", stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取系统统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户增长趋势数据
     */
    @GetMapping("/stats/user-growth")
    public ResponseDTO getUserGrowthStats(@RequestParam(required = false, defaultValue = "6") Integer months) {
        try {
            // 获取最近几个月的用户增长数据
            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            
            LocalDate currentDate = LocalDate.now();
            for (int i = months - 1; i >= 0; i--) {
                LocalDate targetDate = currentDate.minusMonths(i);
                String monthLabel = targetDate.getMonthValue() + "月";
                labels.add(monthLabel);
                
                // 统计该月注册的用户数
                int userCount = userService.getUserCountByMonth(targetDate.getYear(), targetDate.getMonthValue());
                data.add(userCount);
            }
            
            Map<String, Object> growthData = new HashMap<>();
            growthData.put("labels", labels);
            growthData.put("data", data);
            
            return ResponseDTO.success("获取成功", growthData);
        } catch (Exception e) {
            return ResponseDTO.error("获取用户增长趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取运动类型分布数据
     */
    @GetMapping("/stats/activity-type-distribution")
    public ResponseDTO getActivityTypeDistribution() {
        try {
            // 获取所有行为类型
            List<BehaviorType> behaviorTypes = behaviorService.getAllBehaviorTypes();
            
            // 获取每种行为类型的记录数量（最近一个月）
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(1);
            
            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            
            for (BehaviorType type : behaviorTypes) {
                labels.add(type.getTypeName());
                
                // 获取该类型的行为记录数量
                int count = behaviorService.getBehaviorCountByTypeAndDate(type.getTypeId(), startDate, endDate);
                data.add(count);
            }
            
            Map<String, Object> distributionData = new HashMap<>();
            distributionData.put("labels", labels);
            distributionData.put("data", data);
            
            return ResponseDTO.success("获取成功", distributionData);
        } catch (Exception e) {
            return ResponseDTO.error("获取运动类型分布数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新的行为记录
     */
    @GetMapping("/behaviors/recent")
    public ResponseDTO getRecentBehaviors(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            // 获取最新的行为记录
            List<Behavior> recentBehaviors = behaviorService.getRecentBehaviors(limit);
            
            return ResponseDTO.success("获取成功", recentBehaviors);
        } catch (Exception e) {
            return ResponseDTO.error("获取最新行为记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有行为记录（供管理员使用）
     */
    @GetMapping("/behaviors")
    public ResponseDTO getAllBehaviorRecords(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<Behavior> records;
            
            // 如果提供了用户ID，则按用户筛选
            if (userId != null) {
                // 如果提供了日期范围，则按日期范围筛选
                if (startDate != null && endDate != null) {
                    records = behaviorService.getBehaviorRecordsByUserAndDate(userId, startDate, endDate);
                } else {
                    records = behaviorService.getBehaviorRecordsByUser(userId);
                }
                
                // 如果指定了类型ID，则进行过滤
                if (typeId != null) {
                    records = records.stream()
                            .filter(record -> typeId.equals(record.getTypeId()))
                            .collect(Collectors.toList());
                }
            } 
            // 如果没有提供用户ID但提供了其他筛选条件
            else {
                // 获取所有行为记录
                records = behaviorService.getAllBehaviors();
                
                // 应用类型筛选
                if (typeId != null) {
                    records = records.stream()
                            .filter(record -> typeId.equals(record.getTypeId()))
                            .collect(Collectors.toList());
                }
                
                // 应用日期范围筛选
                if (startDate != null && endDate != null) {
                    records = records.stream()
                            .filter(record -> {
                                LocalDate recordDate = record.getRecordDate();
                                return recordDate != null && 
                                       !recordDate.isBefore(startDate) && 
                                       !recordDate.isAfter(endDate);
                            })
                            .collect(Collectors.toList());
                }
            }
            
            return ResponseDTO.success("获取成功", records);
        } catch (Exception e) {
            return ResponseDTO.error("获取行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除行为记录
     */
    @DeleteMapping("/behaviors/{recordId}")
    public ResponseDTO deleteBehaviorRecord(@PathVariable Long recordId) {
        try {
            return behaviorService.deleteBehaviorRecord(recordId);
        } catch (Exception e) {
            return ResponseDTO.error("删除行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有行为类型
     */
    @GetMapping("/behavior-types")
    public ResponseDTO getAllBehaviorTypes() {
        List<BehaviorType> behaviorTypes = behaviorService.getAllBehaviorTypes();
        return ResponseDTO.success("获取成功", behaviorTypes);
    }

    /**
     * 添加行为类型
     */
    @PostMapping("/behavior-types")
    public ResponseDTO addBehaviorType(@RequestBody BehaviorType behaviorType) {
        return behaviorService.addBehaviorType(behaviorType);
    }

    /**
     * 更新行为类型
     */
    @PutMapping("/behavior-types")
    public ResponseDTO updateBehaviorType(@RequestBody BehaviorType behaviorType) {
        return behaviorService.updateBehaviorType(behaviorType);
    }

    /**
     * 删除行为类型
     */
    @DeleteMapping("/behavior-types/{typeId}")
    public ResponseDTO deleteBehaviorType(@PathVariable Integer typeId) {
        return behaviorService.deleteBehaviorType(typeId);
    }

    /**
     * 获取行为类型详情
     */
    @GetMapping("/behavior-types/{typeId}")
    public ResponseDTO getBehaviorType(@PathVariable Integer typeId) {
        BehaviorType behaviorType = behaviorService.getBehaviorTypeById(typeId);
        if (behaviorType != null) {
            return ResponseDTO.success("获取成功", behaviorType);
        } else {
            return ResponseDTO.error("行为类型不存在");
        }
    }

    /**
     * 获取所有徽章
     */
    @GetMapping("/badges")
    public ResponseDTO getAllBadges(
            @RequestParam(required = false) String badgeName,
            @RequestParam(required = false) String badgeType) {
        try {
            List<Badge> badges;
            
            // 如果提供了筛选条件，则进行筛选
            if (badgeName != null || badgeType != null) {
                // 更复杂的筛选逻辑
                badges = badgeService.getAllBadges().stream()
                    .filter(badge -> {
                        boolean nameMatch = badgeName == null || 
                            (badge.getBadgeName() != null && badge.getBadgeName().contains(badgeName));
                        boolean typeMatch = badgeType == null || 
                            (badge.getBadgeType() != null && badge.getBadgeType().equals(badgeType));
                        return nameMatch && typeMatch;
                    })
                    .collect(Collectors.toList());
            } else {
                badges = badgeService.getAllBadges();
            }
            
            // 转换为BadgeDTO以便包含额外的字段
            List<BadgeDTO> badgeDTOs = badges.stream().map(badge -> {
                BadgeDTO dto = new BadgeDTO();
                dto.setBadgeId(badge.getBadgeId());
                dto.setBadgeName(badge.getBadgeName());
                dto.setDescription(badge.getDescription());
                dto.setIconUrl(badge.getIconUrl());
                dto.setConditionType(badge.getConditionType());
                dto.setConditionValue(badge.getConditionValue());
                dto.setLevel(badge.getLevel());
                dto.setRewardPoints(badge.getRewardPoints());
                dto.setStatus(badge.getStatus());
                dto.setBadgeType(badge.getBadgeType()); // 添加徽章类型

                return dto;
            }).collect(Collectors.toList());
            
            return ResponseDTO.success("获取成功", badgeDTOs);
        } catch (Exception e) {
            return ResponseDTO.error("获取徽章列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加徽章
     */
    @PostMapping("/badges")
    public ResponseDTO addBadge(@RequestBody BadgeDTO badgeDTO) {
        Badge badge = new Badge();
        badge.setBadgeName(badgeDTO.getBadgeName());
        badge.setDescription(badgeDTO.getDescription());
        badge.setIconUrl(badgeDTO.getIconUrl());
        badge.setConditionType(badgeDTO.getConditionType());
        badge.setConditionValue(badgeDTO.getConditionValue());
        badge.setLevel(badgeDTO.getLevel());
        badge.setRewardPoints(badgeDTO.getRewardPoints());
        badge.setStatus(badgeDTO.getStatus());
        badge.setBadgeType(badgeDTO.getBadgeType());
        return badgeService.addBadge(badge);
    }

    /**
     * 更新徽章
     */
    @PutMapping("/badges")
    public ResponseDTO updateBadge(@RequestBody BadgeDTO badgeDTO) {
        Badge badge = new Badge();
        badge.setBadgeId(badgeDTO.getBadgeId());
        badge.setBadgeName(badgeDTO.getBadgeName());
        badge.setDescription(badgeDTO.getDescription());
        badge.setIconUrl(badgeDTO.getIconUrl());
        badge.setConditionType(badgeDTO.getConditionType());
        badge.setConditionValue(badgeDTO.getConditionValue());
        badge.setLevel(badgeDTO.getLevel());
        badge.setRewardPoints(badgeDTO.getRewardPoints());
        badge.setStatus(badgeDTO.getStatus());
        badge.setBadgeType(badgeDTO.getBadgeType());

        return badgeService.updateBadge(badge);
    }

    /**
     * 删除徽章
     */
    @DeleteMapping("/badges/{badgeId}")
    public ResponseDTO deleteBadge(@PathVariable Integer badgeId) {
        return badgeService.deleteBadge(badgeId);
    }

    /**
     * 获取徽章详情
     */
    @GetMapping("/badges/{badgeId}")
    public ResponseDTO getBadge(@PathVariable Integer badgeId) {
        Badge badge = badgeService.getBadgeById(badgeId);
        if (badge != null) {
            return ResponseDTO.success("获取成功", badge);
        } else {
            return ResponseDTO.error("徽章不存在");
        }
    }

    /**
     * 上传徽章图标
     */
    @PostMapping("/badges/upload-icon")
    public ResponseDTO uploadBadgeIcon(@RequestParam("file") MultipartFile file) {
        try {
            String iconUrl = fileUploadService.uploadImage(file);
            return ResponseDTO.success("上传成功", iconUrl);
        } catch (Exception e) {
            return ResponseDTO.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/users/{userId}/activity-stats")
    public ResponseDTO getUserActivityStats(@PathVariable Integer userId,
                                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> stats = userService.getUserActivityStats(userId, startDate, endDate);
        return ResponseDTO.success("获取成功", stats);
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseDTO getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseDTO.success("获取成功", users);
    }
    
    /**
     * 根据条件筛选用户
     */
    @GetMapping("/users/search")
    public ResponseDTO searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status) {
        try {
            List<User> users = userService.searchUsers(username, email, status);
            return ResponseDTO.success("获取成功", users);
        } catch (Exception e) {
            return ResponseDTO.error("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseDTO getUser(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return ResponseDTO.success("获取成功", user);
        } else {
            return ResponseDTO.error("用户不存在");
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{userId}")
    public ResponseDTO updateUser(@PathVariable Integer userId, @RequestBody User user) {
        try {
            user.setUserId(userId);
            return userService.updateUserInfo(user);
        } catch (Exception e) {
            return ResponseDTO.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public ResponseDTO updateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Integer> payload) {
        try {
            Integer status = payload.get("status");
            return userService.updateUserStatus(userId, status);
        } catch (Exception e) {
            return ResponseDTO.error("更新用户状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseDTO deleteUser(@PathVariable Integer userId) {
        try {
            return userService.deleteUser(userId);
        } catch (Exception e) {
            return ResponseDTO.error("删除用户失败: " + e.getMessage());
        }
    }
}