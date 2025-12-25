package com.hongyuting.sports.controller.api;

import com.hongyuting.sports.dto.AdminLoginDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.mapper.BadgeMapper;
import com.hongyuting.sports.mapper.BehaviorMapper;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.AdminService;
import com.hongyuting.sports.service.BadgeService;
import com.hongyuting.sports.service.BehaviorService;
import com.hongyuting.sports.service.FileUploadService;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.util.CaptchaUtil;
import com.hongyuting.sports.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

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
    private final com.hongyuting.sports.util.CaptchaUtil captchaUtil;
    private final com.hongyuting.sports.service.OperationLogService operationLogService;
    private final UserMapper userMapper;
    private final BehaviorMapper behaviorMapper;
    private final BadgeMapper badgeMapper;
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseDTO login(@RequestBody AdminLoginDTO loginDTO, HttpServletRequest request, HttpSession session) {
        log.info("收到管理员登录请求，用户名={}", loginDTO.getUsername());
        
        // 验证验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(loginDTO.getCaptcha())) {
            log.warn("管理员登录失败：验证码错误，用户名={}", loginDTO.getUsername());
            return ResponseDTO.error("验证码错误");
        }
        
        String clientIP = getClientIP(request);
        ResponseDTO result = adminService.login(loginDTO, clientIP);
        
        // 登录成功后清除验证码
        if (result.getCode() == 200) {
            session.removeAttribute("captcha");
        }
        
        log.info("管理员登录请求处理完成，结果={}", result.getMessage());
        return result;
    }

    /**
     * 管理员退出登录
     */
    @PostMapping("/logout")
    public ResponseDTO logout(@RequestAttribute Integer adminId,
                              @RequestHeader("Authorization") String authHeader,
                              HttpServletRequest request) {
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String clientIP = getClientIP(request);
        return adminService.logout(adminId, token, clientIP);
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
        if (jwtUtil.validateToken(token)) {
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
        // 权限验证逻辑
        if (!adminService.isSuperAdmin(adminId)) {
            return ResponseDTO.error("无权限创建管理员");
        }
        return adminService.createAdmin(admin);
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public CaptchaUtil getCaptchaUtil() {
        return captchaUtil;
    }

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
    /**
     * 获取操作日志 - 已迁移到OperationLogController以支持所有用户操作日志
     */
    @GetMapping("/logs")
    public ResponseDTO getOperationLogs(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("adminId", adminId);
        params.put("userType", userType);
        params.put("operationType", operationType);
        params.put("operation", operation);
        params.put("targetType", targetType);
        params.put("startDate", startTime);
        params.put("endDate", endTime);
        params.put("offset", (page - 1) * size);
        params.put("limit", size);

        // 获取总记录数
        int totalCount = operationLogService.getOperationLogsCount(params);
        
        // 计算总页数
        int totalPages = size > 0 ? (int) Math.ceil((double) totalCount / size) : 0;
        
        // 获取当前页数据
        List<com.hongyuting.sports.entity.OperationLog> pagedLogs = operationLogService.getOperationLogs(params);
        
        // 构造分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("logs", pagedLogs);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);
        
        return ResponseDTO.success("获取成功", result);
    }

    /**
     * 按目标获取操作日志
     */
    @GetMapping("/logs/target")
    public ResponseDTO getOperationLogsByTarget(@RequestParam String targetType,
                                               @RequestParam(required = false) Integer targetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetType", targetType);
        params.put("targetId", targetId);
        
        List<com.hongyuting.sports.entity.OperationLog> logs = operationLogService.getOperationLogs(params);
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
            int totalUsers = userMapper.selectTotalCount();

            // 获取今日活跃用户数（今天有行为记录的用户数）
            LocalDate today = LocalDate.now();
            int activeToday = behaviorMapper.selectActiveUserCountByDate(today);

            // 获取行为记录总数
            int totalRecords = behaviorMapper.selectTotalCount();

            // 获取徽章总数
            int totalBadges = badgeMapper.selectTotalCount();

            // 构造返回结果
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("activeToday", activeToday);
            stats.put("totalRecords", totalRecords);
            stats.put("totalBadges", totalBadges);

            return ResponseDTO.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取系统统计信息失败: ", e);
            return ResponseDTO.error("获取系统统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户增长趋势数据
     */
    @GetMapping("/stats/user-growth")
    public ResponseDTO getUserGrowthStats(@RequestParam(required = false, defaultValue = "6") Integer months) {
        try {
            // 获取真实用户增长趋势数据
            List<Map<String, Object>> growthDataList = userService.getUserGrowthStats(months);
            
            // 构建前端需要的数据格式
            Map<String, Object> growthData = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            
            for (Map<String, Object> item : growthDataList) {
                labels.add(item.get("month").toString());
                data.add(Integer.valueOf(item.get("userCount").toString()));
            }
            
            growthData.put("labels", labels);
            growthData.put("data", data);
            
            return ResponseDTO.success("获取成功", growthData);
        } catch (Exception e) {
            log.error("获取用户增长趋势数据失败: ", e);
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
            
            // 获取每种行为类型的记录数量（示例数据）
            // 实际应用中应该根据具体时间段统计数据
            LocalDate startDate = LocalDate.now().minusMonths(1); // 最近一个月
            LocalDate endDate = LocalDate.now();
            
            Map<String, Object> distributionData = new HashMap<>();
            distributionData.put("labels", behaviorTypes.stream().map(BehaviorType::getTypeName).toArray());
            
            // 示例数据，实际应从数据库统计得出
            int[] exampleData = {45, 25, 20, 10};
            distributionData.put("data", exampleData);
            
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
     * 获取活跃度排行（按运动时长）
     */
    @GetMapping("/stats/activity-rank")
    public ResponseDTO getActivityRank(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> activityRank = behaviorService.getActivityRank(limit);
            return ResponseDTO.success("获取成功", activityRank);
        } catch (Exception e) {
            return ResponseDTO.error("获取活跃度排行失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件获取行为记录
     */
    @GetMapping("/behaviors")
    public ResponseDTO getBehaviorsByCondition(@RequestParam(required = false) Integer userId,
                                             @RequestParam(required = false) Integer typeId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Behavior> behaviors = List.of();
            
            // 根据不同条件组合查询数据
            if (startDate != null && endDate != null) {
                if (userId != null && typeId != null) {
                    // 按用户ID、类型ID和日期范围查询
                    behaviors = behaviorService.getBehaviorRecordsByUserTypeAndDate(userId, typeId, startDate, endDate);
                } else if (userId != null) {
                    // 按用户ID和日期范围查询
                    behaviors = behaviorService.getBehaviorRecordsByUserAndDate(userId, startDate, endDate);
                } else if (typeId != null) {
                    // 按类型ID和日期范围查询
                    behaviors = behaviorService.getBehaviorRecordsByTypeAndDate(typeId, startDate, endDate);
                } else {
                    // 按日期范围查询
                    behaviors = behaviorService.getBehaviorRecordsByDate(startDate, endDate);
                }
            } else if (userId != null && typeId != null) {
                // 按用户ID和类型ID查询
                behaviors = behaviorService.getBehaviorRecordsByUserAndType(userId, typeId);
            } else if (userId != null) {
                // 按用户ID查询
                behaviors = behaviorService.getBehaviorRecordsByUser(userId);
            } else if (typeId != null) {
                // 按类型ID查询
                behaviors = behaviorService.getBehaviorRecordsByType(typeId);
            } else {
                // 获取所有行为记录
                behaviors = behaviorService.getAllBehaviors();
            }
            
            return ResponseDTO.success("获取成功", behaviors);
        } catch (Exception e) {
            return ResponseDTO.error("获取行为记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除行为记录
     */
    @DeleteMapping("/behaviors/{recordId}")
    public ResponseDTO deleteBehavior(@PathVariable Long recordId) {
        return behaviorService.deleteBehaviorRecord(recordId);
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
    public ResponseDTO getAllBadges(@RequestParam(required = false) String badgeName,
                                   @RequestParam(required = false) String badgeType) {
        try {
            List<Badge> badges;
            if (StringUtils.hasText(badgeName) || StringUtils.hasText(badgeType)) {
                // 如果提供了搜索条件，执行搜索
                badges = searchBadges(badgeName, badgeType);
            } else {
                // 否则获取所有徽章
                badges = badgeService.getAllBadges();
            }
            return ResponseDTO.success("获取成功", badges);
        } catch (Exception e) {
            return ResponseDTO.error("获取徽章失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据条件搜索徽章
     */
    private List<Badge> searchBadges(String badgeName, String badgeType) {
        List<Badge> result = new ArrayList<>();
        
        if (StringUtils.hasText(badgeName) && StringUtils.hasText(badgeType)) {
            // 同时按名称和类型搜索
            List<Badge> badgesByName = badgeService.getBadgesByName(badgeName);
            if (badgesByName != null) {
                result = badgesByName.stream()
                        .filter(badge -> badgeType.equals(badge.getBadgeType()))
                        .collect(Collectors.toList());
            }
        } else if (StringUtils.hasText(badgeName)) {
            // 按名称搜索
            result = badgeService.getBadgesByName(badgeName);
        } else if (StringUtils.hasText(badgeType)) {
            // 按类型搜索
            result = badgeService.getBadgesByType(badgeType);
        }
        
        return result != null ? result : new ArrayList<>();
    }

    /**
     * 添加徽章
     */
    @PostMapping("/badges")
    public ResponseDTO addBadge(@RequestBody Badge badge) {
        return badgeService.addBadge(badge);
    }

    /**
     * 更新徽章
     */
    @PutMapping("/badges")
    public ResponseDTO updateBadge(@RequestBody Badge badge) {
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
    public ResponseDTO getAllUsers(@RequestParam(required = false) String username,
                                  @RequestParam(required = false) String email,
                                  @RequestParam(required = false) Integer status) {
        List<User> users;
        if (org.springframework.util.StringUtils.hasText(username) || 
            org.springframework.util.StringUtils.hasText(email) || 
            status != null) {
            // 如果提供了搜索条件，执行搜索
            users = userService.searchUsers(username, email, status);
        } else {
            // 否则获取所有用户
            users = userService.getAllUsers();
        }
        
        // 隐藏密码信息
        if (users != null) {
            users.forEach(user -> {
                user.setPassword(null);
                user.setSalt(null);
            });
        }
        
        return ResponseDTO.success("获取成功", users);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseDTO getUser(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            // 隐藏密码信息
            user.setPassword(null);
            user.setSalt(null);
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
        if (userId == null || user.getUserId() == null || !userId.equals(user.getUserId())) {
            return ResponseDTO.error("用户ID不匹配");
        }
        
        user.setUserId(userId);
        return userService.updateUserInfo(user);
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public ResponseDTO updateUserStatus(@PathVariable Integer userId, @RequestParam Integer status) {
        return userService.updateUserStatus(userId, status);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseDTO deleteUser(@PathVariable Integer userId) {
        return userService.deleteUser(userId);
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/users/check-username")
    public ResponseDTO checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return ResponseDTO.success("查询成功", exists);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/users/check-email")
    public ResponseDTO checkEmailExists(@RequestParam String email) {
        boolean exists = userService.checkEmailExists(email);
        return ResponseDTO.success("查询成功", exists);
    }
}