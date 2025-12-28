package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.AdminLoginDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.entity.OperationLog;
import com.hongyuting.sports.mapper.AdminLogMapper;
import com.hongyuting.sports.mapper.AdminMapper;
import com.hongyuting.sports.service.AdminService;
import com.hongyuting.sports.service.OperationLogService;
import com.hongyuting.sports.service.TokenService;
import com.hongyuting.sports.util.JwtUtil;
import com.hongyuting.sports.util.SaltUtil;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.mapper.BehaviorMapper;
import com.hongyuting.sports.mapper.BadgeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * 管理员服务实现类
 */
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final AdminLogMapper adminLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SaltUtil saltUtil;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;
    private final BehaviorMapper behaviorMapper;
    private final BadgeMapper badgeMapper;

    @Override
    public ResponseDTO login(AdminLoginDTO loginDTO, String clientIP) {
        try {
            // 1. 参数校验
            if (loginDTO == null) {
                return ResponseDTO.error("登录信息不能为空");
            }
            
            if (!StringUtils.hasText(loginDTO.getUsername())) {
                return ResponseDTO.error("用户名不能为空");
            }
            
            if (!StringUtils.hasText(loginDTO.getPassword())) {
                return ResponseDTO.error("密码不能为空");
            }

            // 2. 根据用户名查询管理员
            Admin admin = adminMapper.findByUsername(loginDTO.getUsername());
            if (admin == null) {
                log.warn("管理员登录失败：用户名或密码错误，用户名={}", loginDTO.getUsername());
                
                // 记录失败的登录尝试
                try {
                    AdminLog adminLog = new AdminLog();
                    adminLog.setAdminId(null); // 未成功登录，ID为null
                    adminLog.setOperation("管理员登录失败");
                    adminLog.setTargetType("SYSTEM");
                    adminLog.setDetail("用户名或密码错误: " + loginDTO.getUsername());
                    adminLog.setIpAddress(clientIP);
                    adminLog.setOperationTime(LocalDateTime.now());
                    adminLogMapper.insert(adminLog);
                } catch (Exception logException) {
                    log.error("记录管理员登录失败日志异常", logException);
                }
                
                return ResponseDTO.error("用户名或密码错误");
            }

            // 3. 检查账号状态
            if (admin.getStatus() == null || admin.getStatus() == 0) {
                log.warn("管理员登录失败：账号已被禁用，管理员ID={}", admin.getAdminId());
                
                // 记录失败的登录尝试
                try {
                    AdminLog adminLog = new AdminLog();
                    adminLog.setAdminId(admin.getAdminId());
                    adminLog.setOperation("管理员登录失败");
                    adminLog.setTargetType("SYSTEM");
                    adminLog.setDetail("账号已被禁用");
                    adminLog.setIpAddress(clientIP);
                    adminLog.setOperationTime(LocalDateTime.now());
                    adminLogMapper.insert(adminLog);
                } catch (Exception logException) {
                    log.error("记录管理员登录失败日志异常", logException);
                }
                
                return ResponseDTO.error("账号已被禁用");
            }

            // 4. 验证密码
            boolean passwordValid = false;
            boolean needToUpdatePassword = false;
            String encryptedPassword = admin.getPassword();
            String salt = admin.getSalt();
            
            if (admin.getSalt() == null || admin.getSalt().isEmpty()) {
                // 明文密码比较
                passwordValid = loginDTO.getPassword().equals(admin.getPassword());
                // 如果验证通过且是明文密码，标记需要更新为加密密码
                if (passwordValid) {
                    needToUpdatePassword = true;
                }
            } else {
                // 加盐加密验证
                passwordValid = saltUtil.verifyPassword(loginDTO.getPassword(), admin.getPassword(), admin.getSalt());
            }

            if (!passwordValid) {
                log.warn("管理员登录失败：用户名或密码错误，管理员ID={}", admin.getAdminId());
                
                // 记录失败的登录尝试
                try {
                    AdminLog adminLog = new AdminLog();
                    adminLog.setAdminId(admin.getAdminId());
                    adminLog.setOperation("管理员登录失败");
                    adminLog.setTargetType("SYSTEM");
                    adminLog.setDetail("用户名或密码错误");
                    adminLog.setIpAddress(clientIP);
                    adminLog.setOperationTime(LocalDateTime.now());
                    adminLogMapper.insert(adminLog);
                } catch (Exception logException) {
                    log.error("记录管理员登录失败日志异常", logException);
                }
                
                return ResponseDTO.error("用户名或密码错误");
            }

            // 5. 如果是明文密码登录，生成盐值并加密密码
            if (needToUpdatePassword) {
                salt = saltUtil.generateSalt();
                encryptedPassword = saltUtil.encryptPassword(loginDTO.getPassword(), salt);
                
                // 更新数据库中的密码和盐值
                adminMapper.updatePasswordAndSalt(admin.getAdminId(), encryptedPassword, salt);
                
                // 更新内存中的admin对象
                admin.setSalt(salt);
                admin.setPassword(encryptedPassword);
            }

            // 6. 生成JWT Token
            String token = jwtUtil.generateToken(admin.getAdminId());
            String refreshToken = jwtUtil.generateRefreshToken(admin.getAdminId());

            // 7. 将管理员信息存储到Redis
            tokenService.storeAdminInfo(token, admin);

            // 8. 更新最后登录时间
            admin.setLastLoginTime(LocalDateTime.now());
            adminMapper.updateLastLoginTime(admin.getAdminId(), admin.getLastLoginTime());

            // 9. 记录登录日志
            AdminLog adminLog = new AdminLog();
            adminLog.setAdminId(admin.getAdminId());
            adminLog.setOperation("管理员登录");
            adminLog.setTargetType("SYSTEM");
            adminLog.setDetail("IP: " + clientIP);
            adminLog.setIpAddress(clientIP);
            adminLog.setOperationTime(LocalDateTime.now());
            adminLogMapper.insert(adminLog);

            // 10. 返回登录结果（不返回密码信息）
            admin.setPassword(null);
            admin.setSalt(null);

            AdminService.LoginResult loginResult = new AdminService.LoginResult();
            loginResult.setAdmin(admin);
            loginResult.setToken(token);
            loginResult.setRefreshToken(refreshToken);

            log.info("管理员登录成功：管理员ID={}", admin.getAdminId());
            return ResponseDTO.success("登录成功", loginResult);
        } catch (Exception e) {
            log.error("管理员登录异常：", e);
            return ResponseDTO.error("登录异常，请稍后重试");
        }
    }

    @Override
    public ResponseDTO logout(Integer adminId, String token) {
        // 默认IP地址，用于向后兼容
        return logout(adminId, token, "0:0:0:0:0:0:0:1");
    }

    @Override
    public ResponseDTO logout(Integer adminId, String token, String clientIP) {
        try {
            // 参数校验
            if (adminId == null) {
                return ResponseDTO.error("管理员ID不能为空");
            }
            
            if (!StringUtils.hasText(token)) {
                return ResponseDTO.error("Token不能为空");
            }
            
            // 验证JWT Token格式是否有效
            if (!jwtUtil.validateToken(token)) {
                return ResponseDTO.error("Token格式无效");
            }

            // 从Redis中删除Token
            tokenService.deleteAdminToken(token);
            
            // 记录退出日志
            try {
                OperationLog logoutLog = new OperationLog();
                logoutLog.setAdminId(adminId);
                logoutLog.setUserType("ADMIN");
                logoutLog.setOperation("管理员退出");
                logoutLog.setOperationType("AUTH");
                logoutLog.setTargetType("SYSTEM");
                logoutLog.setIpAddress(clientIP); // 使用传入的IP地址
                logoutLog.setOperationTime(LocalDateTime.now());
                logoutLog.setDetail("管理员退出登录");
                
                operationLogService.addOperationLog(logoutLog);
            } catch (Exception logException) {
                log.error("记录管理员退出日志异常", logException);
            }
            
            log.info("管理员退出登录成功：管理员ID={}", adminId);
            return ResponseDTO.success("退出成功");
        } catch (Exception e) {
            log.error("管理员退出登录异常：管理员ID={}", adminId, e);
            return ResponseDTO.error("退出异常，请稍后重试");
        }
    }

    @Override
    public ResponseDTO refreshAccessToken(String refreshToken) {
        try {
            // 参数校验
            if (!StringUtils.hasText(refreshToken)) {
                return ResponseDTO.error("刷新令牌不能为空");
            }
            
            if (!jwtUtil.validateToken(refreshToken)) {
                return ResponseDTO.error("刷新令牌无效");
            }

            Integer adminId = jwtUtil.getUserIdFromToken(refreshToken);
            if (adminId == null) {
                return ResponseDTO.error("刷新令牌无效");
            }

            // 验证刷新令牌是否在Redis中
            String refreshTokenKey = "admin:refresh_token:" + refreshToken;
            String storedUsername = (String) redisTemplate.opsForValue().get(refreshTokenKey);

            if (storedUsername == null) {
                return ResponseDTO.error("刷新令牌无效");
            }

            // 生成新的Token
            String newToken = jwtUtil.generateToken(adminId);
            String newRefreshToken = jwtUtil.generateRefreshToken(adminId);

            // 删除旧的管理员信息
            tokenService.deleteAdminToken(refreshToken);
            
            // 创建新的管理员信息
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null) {
                admin.setPassword(null); // 不存储密码信息
                admin.setSalt(null); // 不存储盐值
                tokenService.storeAdminInfo(newToken, admin);
            }

            // 构造返回结果
            Map<String, String> result = new HashMap<>();
            result.put("token", newToken);
            result.put("refreshToken", newRefreshToken);

            log.info("管理员刷新令牌成功：管理员ID={}", adminId);
            return ResponseDTO.success("刷新令牌成功", result);
        } catch (Exception e) {
            log.error("管理员刷新令牌异常：", e);
            return ResponseDTO.error("刷新令牌异常，请稍后重试");
        }
    }

    @Override
    public boolean existsToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return true;
            }

            // 验证管理员Token是否存在
            return tokenService.existsAdminToken(token);
        } catch (Exception e) {
            log.error("验证管理员Token异常：", e);
            return true;
        }
    }

    @Override
    public void refreshToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return;
            }

            // 刷新管理员token过期时间
            tokenService.refreshAdminToken(token);
        } catch (Exception e) {
            log.error("刷新管理员Token异常：", e);
        }
    }

    @Override
    public Admin getAdminById(Integer adminId) {
        try {
            if (adminId == null) {
                return null;
            }
            return adminMapper.selectById(adminId);
        } catch (Exception e) {
            log.error("获取管理员信息异常：管理员ID={}", adminId, e);
            return null;
        }
    }

    @Override
    public ResponseDTO changePassword(Integer adminId, String oldPassword, String newPassword) {
        try {
            // 参数校验
            if (adminId == null) {
                return ResponseDTO.error("管理员ID不能为空");
            }
            
            if (!StringUtils.hasText(oldPassword)) {
                return ResponseDTO.error("原密码不能为空");
            }
            
            if (!StringUtils.hasText(newPassword)) {
                return ResponseDTO.error("新密码不能为空");
            }
            
            if (newPassword.length() < 6) {
                return ResponseDTO.error("新密码长度不能少于6位");
            }

            // 查询管理员信息
            Admin admin = adminMapper.selectById(adminId);
            if (admin == null) {
                return ResponseDTO.error("管理员不存在");
            }

            // 验证原密码
            boolean oldPasswordValid = false;
            if (admin.getSalt() == null || admin.getSalt().isEmpty()) {
                // 明文密码比较
                oldPasswordValid = oldPassword.equals(admin.getPassword());
            } else {
                // 加盐加密验证
                oldPasswordValid = saltUtil.verifyPassword(oldPassword, admin.getPassword(), admin.getSalt());
            }

            if (!oldPasswordValid) {
                return ResponseDTO.error("原密码错误");
            }

            // 生成新的盐值和加密密码
            String newSalt = saltUtil.generateSalt();
            String newEncryptedPassword = saltUtil.encryptPassword(newPassword, newSalt);

            // 更新密码
            int result = adminMapper.updatePasswordAndSalt(adminId, newEncryptedPassword, newSalt);
            if (result > 0) {
                // 清除Redis中的Token
                // 删除与该管理员相关的token
                tokenService.deleteAdminTokenByUserId(adminId);
                
                log.info("管理员密码修改成功：管理员ID={}", adminId);
                return ResponseDTO.success("密码修改成功，请重新登录");
            } else {
                log.warn("管理员密码修改失败：管理员ID={}", adminId);
                return ResponseDTO.error("密码修改失败");
            }
        } catch (Exception e) {
            log.error("管理员密码修改异常：管理员ID={}", adminId, e);
            return ResponseDTO.error("密码修改异常: " + e.getMessage());
        }
    }

    @Override
    public List<AdminLog> getAdminLogs(Integer adminId, String operation, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return getAdminLogsByCondition(adminId, operation, startTime, endTime, null);
        } catch (Exception e) {
            log.error("获取管理员操作日志异常：", e);
            return null;
        }
    }
    
    public List<AdminLog> getAdminLogsByCondition(Integer adminId, String operation, LocalDateTime startTime, LocalDateTime endTime, String logLevel) {
        try {
            // 为了向后兼容，仍然使用原有的adminLogMapper获取管理员日志
            return adminLogMapper.selectByCondition(adminId, operation, startTime, endTime, logLevel);
        } catch (Exception e) {
            log.error("根据条件获取管理员操作日志异常：", e);
            return null;
        }
    }
    
    /**
     * 获取所有操作日志（包括用户和管理员）
     */
    public List<com.hongyuting.sports.entity.OperationLog> getOperationLogsByCondition(Integer userId, Integer adminId, String operation, LocalDateTime startTime, LocalDateTime endTime, String operationType, String userType) {
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("userId", userId);
            params.put("adminId", adminId);
            params.put("operation", operation);
            params.put("startDate", startTime);
            params.put("endDate", endTime);
            params.put("operationType", operationType);
            params.put("userType", userType);
            
            return operationLogService.getOperationLogs(params);
        } catch (Exception e) {
            log.error("根据条件获取所有操作日志异常：", e);
            return null;
        }
    }

    @Override
    public List<AdminLog> getAdminLogsByTarget(String targetType, Integer targetId) {
        try {
            return adminLogMapper.selectByTarget(targetType, targetId);
        } catch (Exception e) {
            log.error("按目标获取管理员操作日志异常：", e);
            return null;
        }
    }

    @Override
    public ResponseDTO addAdminLog(AdminLog adminLog) {
        try {
            if (adminLog == null) {
                return ResponseDTO.error("日志信息不能为空");
            }

            int result = adminLogMapper.insert(adminLog);
            if (result > 0) {
                log.info("添加管理员操作日志成功：日志ID={}", adminLog.getId());
                return ResponseDTO.success("添加日志成功");
            } else {
                log.warn("添加管理员操作日志失败");
                return ResponseDTO.error("添加日志失败");
            }
        } catch (Exception e) {
            log.error("添加管理员操作日志异常：", e);
            return ResponseDTO.error("添加日志异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO clearOldLogs(LocalDateTime beforeTime) {
        try {
            if (beforeTime == null) {
                return ResponseDTO.error("时间不能为空");
            }

            int result = adminLogMapper.deleteBeforeTime(beforeTime);
            log.info("清理管理员旧日志完成：清理条数={}, 时间={}", result, beforeTime);
            return ResponseDTO.success("清理完成，共清理" + result + "条日志");
        } catch (Exception e) {
            log.error("清理管理员旧日志异常：", e);
            return ResponseDTO.error("清理日志异常: " + e.getMessage());
        }
    }

    @Override
    public Integer getAdminOperationCount(Integer adminId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (adminId == null || startTime == null || endTime == null) {
                return 0;
            }
            return adminLogMapper.selectOperationCount(adminId, startTime, endTime);
        } catch (Exception e) {
            log.error("获取管理员操作次数异常：", e);
            return 0;
        }
    }

    @Override
    public List<Admin> getAllAdmins() {
        try {
            return adminMapper.selectAll();
        } catch (Exception e) {
            log.error("获取所有管理员异常：", e);
            return List.of();
        }
    }

    @Override
    public ResponseDTO updateAdminInfo(Admin admin) {
        try {
            // 参数校验
            if (admin == null || admin.getAdminId() == null) {
                return ResponseDTO.error("管理员信息不能为空");
            }
            
            // 检查管理员是否存在
            Admin existingAdmin = adminMapper.selectById(admin.getAdminId());
            if (existingAdmin == null) {
                return ResponseDTO.error("管理员不存在");
            }
            
            // 更新管理员信息（不更新密码和盐值）
            existingAdmin.setUsername(admin.getUsername());
            existingAdmin.setEmail(admin.getEmail());
            existingAdmin.setRoleLevel(admin.getRoleLevel());
            existingAdmin.setDepartment(admin.getDepartment());
            existingAdmin.setStatus(admin.getStatus());
            
            int result = adminMapper.updateAdminInfo(existingAdmin);
            if (result > 0) {
                log.info("更新管理员信息成功：管理员ID={}", admin.getAdminId());
                
                // 更新Redis中的管理员信息
                String token = (String) redisTemplate.opsForValue().get("admin:id:" + admin.getAdminId());
                if (token != null) {
                    existingAdmin.setPassword(null); // 不返回密码
                    existingAdmin.setSalt(null); // 不返回盐值
                    tokenService.storeAdminInfo(token, existingAdmin);
                }
                
                return ResponseDTO.success("更新管理员信息成功");
            } else {
                log.warn("更新管理员信息失败：管理员ID={}", admin.getAdminId());
                return ResponseDTO.error("更新管理员信息失败");
            }
        } catch (Exception e) {
            log.error("更新管理员信息异常：", e);
            return ResponseDTO.error("更新管理员信息异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO deleteAdmin(Integer adminId) {
        try {
            // 参数校验
            if (adminId == null) {
                return ResponseDTO.error("管理员ID不能为空");
            }
            
            // 检查管理员是否存在
            Admin admin = adminMapper.selectById(adminId);
            if (admin == null) {
                return ResponseDTO.error("管理员不存在");
            }
            
            // 不允许删除自己（如果是通过token获取的管理员ID）
            // 注意：这里需要通过调用方来判断是否为当前登录管理员
            
            // 删除管理员
            int result = adminMapper.deleteById(adminId);
            if (result > 0) {
                log.info("删除管理员成功：管理员ID={}", adminId);
                
                // 删除Redis中的相关数据
                String token = (String) redisTemplate.opsForValue().get("admin:id:" + adminId);
                if (token != null) {
                    tokenService.deleteAdminToken(token);
                }
                
                // 删除管理员的所有操作日志
                adminLogMapper.deleteByAdminId(adminId);
                
                return ResponseDTO.success("删除管理员成功");
            } else {
                log.warn("删除管理员失败：管理员ID={}", adminId);
                return ResponseDTO.error("删除管理员失败");
            }
        } catch (Exception e) {
            log.error("删除管理员异常：", e);
            return ResponseDTO.error("删除管理员异常: " + e.getMessage());
        }
    }

    @Override
    public List<String> getTokensByAdminId(Integer adminId) {
        if (adminId == null) {
            return List.of();
        }
        
        // 从Redis中获取管理员ID到token的映射
        String token = (String) redisTemplate.opsForValue().get("admin:id:" + adminId);
        
        if (token != null) {
            return List.of(token);
        }
        
        return List.of();
    }

    @Override
    public ResponseDTO createAdmin(Admin admin) {
        try {
            // 参数校验
            if (admin == null) {
                return ResponseDTO.error("管理员信息不能为空");
            }
            
            if (!StringUtils.hasText(admin.getUsername())) {
                return ResponseDTO.error("用户名不能为空");
            }
            
            if (!StringUtils.hasText(admin.getPassword())) {
                return ResponseDTO.error("密码不能为空");
            }
            
            if (admin.getPassword().length() < 6) {
                return ResponseDTO.error("密码长度不能少于6位");
            }

            // 检查用户名是否已存在
            Admin existingAdmin = adminMapper.findByUsername(admin.getUsername());
            if (existingAdmin != null) {
                return ResponseDTO.error("用户名已存在");
            }

            // 生成盐值并加密密码
            String salt = saltUtil.generateSalt();
            String encryptedPassword = saltUtil.encryptPassword(admin.getPassword(), salt);
            
            // 设置默认值
            admin.setPassword(encryptedPassword);
            admin.setSalt(salt);
            admin.setStatus(1); // 默认启用
            admin.setRoleLevel(2); // 默认普通管理员，1为超级管理员
            admin.setCreatedAt(LocalDateTime.now());
            admin.setLastLoginTime(null);

            // 插入管理员
            int result = adminMapper.insert(admin);
            if (result > 0) {
                log.info("创建管理员成功：管理员ID={}", admin.getAdminId());
                
                // 返回创建的管理员信息（不包含密码和盐值）
                Admin createdAdmin = new Admin();
                createdAdmin.setAdminId(admin.getAdminId());
                createdAdmin.setUsername(admin.getUsername());
                createdAdmin.setStatus(admin.getStatus());
                createdAdmin.setRoleLevel(admin.getRoleLevel());
                createdAdmin.setCreatedAt(admin.getCreatedAt());
                
                return ResponseDTO.success("创建管理员成功", createdAdmin);
            } else {
                log.warn("创建管理员失败：用户名={}", admin.getUsername());
                return ResponseDTO.error("创建管理员失败");
            }
        } catch (Exception e) {
            log.error("创建管理员异常：", e);
            return ResponseDTO.error("创建管理员异常: " + e.getMessage());
        }
    }

    @Override
    public boolean isSuperAdmin(Integer adminId) {
        // 获取管理员信息
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            return false;
        }
        
        // 假设roleLevel为1表示超级管理员，可以根据实际需求调整判断逻辑
        // 或者可以有其他判断方式，比如通过角色字段
        return admin.getRoleLevel() != null && admin.getRoleLevel() == 1;
    }

    @Override
    public Object getSystemStats() {
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

            return stats;
        } catch (Exception e) {
            log.error("获取系统统计信息失败: ", e);
            return null;
        }
    }
}