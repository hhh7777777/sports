package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.AdminLoginDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.mapper.AdminMapper;
import com.hongyuting.sports.service.AdminService;
import com.hongyuting.sports.util.JwtUtil;
import com.hongyuting.sports.util.SaltUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 管理员服务实现类
 */
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SaltUtil saltUtil;
    private final JwtUtil jwtUtil;

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
                return ResponseDTO.error("用户名或密码错误");
            }

            // 3. 检查账号状态
            if (admin.getStatus() == null || admin.getStatus() == 0) {
                log.warn("管理员登录失败：账号已被禁用，管理员ID={}", admin.getId());
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
                log.warn("管理员登录失败：用户名或密码错误，管理员ID={}", admin.getId());
                return ResponseDTO.error("用户名或密码错误");
            }

            // 5. 如果是明文密码登录，生成盐值并加密密码
            if (needToUpdatePassword) {
                salt = saltUtil.generateSalt();
                encryptedPassword = saltUtil.encryptPassword(loginDTO.getPassword(), salt);
                
                // 更新数据库中的密码和盐值
                adminMapper.updatePasswordAndSalt(admin.getId(), encryptedPassword, salt);
                
                // 更新内存中的admin对象
                admin.setSalt(salt);
                admin.setPassword(encryptedPassword);
            }

            // 6. 生成JWT Token
            String token = jwtUtil.generateToken(admin.getId());
            String refreshToken = jwtUtil.generateRefreshToken(admin.getId());

            // 7. 将Token存储到Redis
            String tokenKey = "admin:token:" + admin.getId();
            String refreshTokenKey = "admin:refresh_token:" + admin.getId();

            redisTemplate.opsForValue().set(tokenKey, token, 24, TimeUnit.HOURS); // Token 24小时
            redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 30, TimeUnit.DAYS); // 刷新Token 30天

            // 8. 更新最后登录时间
            admin.setLastLoginTime(LocalDateTime.now());
            adminMapper.updateLastLoginTime(admin.getId(), admin.getLastLoginTime());

            // 9. 记录登录日志
            AdminLog adminLog = new AdminLog();
            adminLog.setAdminId(admin.getId());
            adminLog.setOperation("管理员登录");
            adminLog.setTargetType("SYSTEM");
            adminLog.setDetail("IP: " + clientIP);
            adminLog.setIpAddress(clientIP);
            adminLog.setOperationTime(LocalDateTime.now());
            adminMapper.insertAdminLog(adminLog);

            // 10. 返回登录结果（不返回密码信息）
            admin.setPassword(null);
            admin.setSalt(null);

            AdminService.LoginResult loginResult = new AdminService.LoginResult();
            loginResult.setAdmin(admin);
            loginResult.setToken(token);
            loginResult.setRefreshToken(refreshToken);

            log.info("管理员登录成功：管理员ID={}", admin.getId());
            return ResponseDTO.success("登录成功", loginResult);
        } catch (Exception e) {
            log.error("管理员登录异常：", e);
            return ResponseDTO.error("登录异常，请稍后重试");
        }
    }

    @Override
    public ResponseDTO logout(Integer adminId, String token) {
        try {
            // 参数校验
            if (adminId == null) {
                return ResponseDTO.error("管理员ID不能为空");
            }
            
            if (!StringUtils.hasText(token)) {
                return ResponseDTO.error("Token不能为空");
            }

            // 从Redis中删除Token
            String tokenKey = "admin:token:" + adminId;
            String refreshTokenKey = "admin:refresh_token:" + adminId;

            redisTemplate.delete(tokenKey);
            redisTemplate.delete(refreshTokenKey);
            
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
            String refreshTokenKey = "admin:refresh_token:" + adminId;
            String storedRefreshToken = (String) redisTemplate.opsForValue().get(refreshTokenKey);

            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                return ResponseDTO.error("刷新令牌无效");
            }

            // 生成新的Token
            String newToken = jwtUtil.generateToken(adminId);
            String newRefreshToken = jwtUtil.generateRefreshToken(adminId);
            String tokenKey = "admin:token:" + adminId;
            String newRefreshTokenKey = "admin:refresh_token:" + adminId;

            // 更新Redis中的Token
            redisTemplate.opsForValue().set(tokenKey, newToken, 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(newRefreshTokenKey, newRefreshToken, 30, TimeUnit.DAYS);

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
                return false;
            }

            // 从Token中解析管理员ID
            Integer adminId = jwtUtil.getUserIdFromToken(token);
            if (adminId == null) {
                return false;
            }

            // 验证Token是否在Redis中
            String tokenKey = "admin:token:" + adminId;
            String storedToken = (String) redisTemplate.opsForValue().get(tokenKey);

            return storedToken != null && storedToken.equals(token);
        } catch (Exception e) {
            log.error("验证管理员Token异常：", e);
            return false;
        }
    }

    @Override
    public void refreshToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return;
            }

            // 从Token中解析管理员ID
            Integer adminId = jwtUtil.getUserIdFromToken(token);
            if (adminId == null) {
                return;
            }

            // 验证Token是否在Redis中
            String tokenKey = "admin:token:" + adminId;
            String storedToken = (String) redisTemplate.opsForValue().get(tokenKey);

            if (storedToken != null && storedToken.equals(token)) {
                // 延长Token过期时间
                redisTemplate.expire(tokenKey, 24, TimeUnit.HOURS);
            }
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
            return adminMapper.findById(adminId);
        } catch (Exception e) {
            log.error("获取管理员信息异常：管理员ID={}", adminId, e);
            return null;
        }
    }

    @Override
    @Transactional
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
            if (adminMapper.findByUsername(admin.getUsername()) != null) {
                return ResponseDTO.error("用户名已存在");
            }

            // 生成盐值
            String salt = saltUtil.generateSalt();
            admin.setSalt(salt);

            // 加密密码
            String encryptedPassword = saltUtil.encryptPassword(admin.getPassword(), salt);
            admin.setPassword(encryptedPassword);

            // 设置默认状态
            if (admin.getStatus() == null) {
                admin.setStatus(1);
            }

            admin.setCreatedAt(LocalDateTime.now());

            int result = adminMapper.insert(admin);
            if (result > 0) {
                log.info("管理员创建成功：管理员ID={}", admin.getId());
                return ResponseDTO.success("管理员创建成功");
            } else {
                log.warn("管理员创建失败：用户名={}", admin.getUsername());
                return ResponseDTO.error("管理员创建失败");
            }
        } catch (Exception e) {
            log.error("管理员创建异常：", e);
            return ResponseDTO.error("管理员创建异常: " + e.getMessage());
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
            Admin admin = adminMapper.findById(adminId);
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
                String tokenKey = "admin:token:" + adminId;
                String refreshTokenKey = "admin:refresh_token:" + adminId;
                redisTemplate.delete(tokenKey);
                redisTemplate.delete(refreshTokenKey);
                
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
            return adminMapper.findAdminLogs(adminId, operation, startTime, endTime);
        } catch (Exception e) {
            log.error("获取管理员操作日志异常：", e);
            return null;
        }
    }

    @Override
    public List<AdminLog> getAdminLogsByTarget(String targetType, Integer targetId) {
        try {
            return adminMapper.findAdminLogsByTarget(targetType, targetId);
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

            int result = adminMapper.insertAdminLog(adminLog);
            if (result > 0) {
                log.info("添加管理员操作日志成功：日志ID={}", adminLog.getLogId());
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

            int result = adminMapper.deleteAdminLogsBefore(beforeTime);
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
            return adminMapper.selectOperationCount(adminId, startTime, endTime);
        } catch (Exception e) {
            log.error("获取管理员操作次数异常：", e);
            return 0;
        }
    }

    @Override
    public List<Admin> getAllAdmins() {
        // 这个方法在AdminMapper中没有对应的方法，暂时返回空列表
        return List.of();
    }

    @Override
    public ResponseDTO updateAdminInfo(Admin admin) {
        // 这个方法在AdminMapper中没有对应的方法，暂时返回错误
        return ResponseDTO.error("暂不支持更新管理员信息");
    }

    @Override
    public ResponseDTO deleteAdmin(Integer adminId) {
        // 这个方法在AdminMapper中没有对应的方法，暂时返回错误
        return ResponseDTO.error("暂不支持删除管理员");
    }
}