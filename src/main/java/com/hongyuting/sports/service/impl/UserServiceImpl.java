package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.entity.UserStats;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.TokenService;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.util.JwtUtil;
import com.hongyuting.sports.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenService tokenService;

    @Override
    @Transactional
    public ResponseDTO register(RegisterDTO registerDTO) {
        try {
            // 检查用户名是否存在
            if (userMapper.existsByUsername(registerDTO.getUsername())) {
                return ResponseDTO.error("用户名已存在");
            }

            // 检查邮箱是否存在
            if (userMapper.existsByEmail(registerDTO.getEmail())) {
                return ResponseDTO.error("邮箱已被注册");
            }

            // 创建用户对象
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setNickname(registerDTO.getUsername()); // 默认使用用户名作为昵称
            user.setEmail(registerDTO.getEmail());

            // 生成盐值并加密密码
            String salt = PasswordUtil.generateSalt();
            String encryptedPassword = PasswordUtil.encryptPassword(registerDTO.getPassword(), salt);

            user.setPassword(encryptedPassword);
            user.setSalt(salt);
            user.setUserStatus(1); // 1表示活跃用户
            user.setRegisterTime(LocalDateTime.now());
            user.setLastLoginTime(LocalDateTime.now());

            int result = userMapper.insertUser(user);
            if (result > 0) {
                // 注册成功，返回用户信息（不包含敏感信息）
                user.setPassword(null);
                user.setSalt(null);
                return ResponseDTO.success("注册成功", user);
            } else {
                return ResponseDTO.error("注册失败");
            }
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return ResponseDTO.error("注册异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO login(LoginDTO loginDTO) {
        try {
            User user = userMapper.selectUserByUsername(loginDTO.getUsername());
            if (user == null) {
                return ResponseDTO.error("用户名或密码错误");
            }

            // 验证密码
            if (!PasswordUtil.validatePassword(loginDTO.getPassword(), user.getSalt(), user.getPassword())) {
                return ResponseDTO.error("用户名或密码错误");
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateUser(user);

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
            
            // 存储用户信息到Redis
            tokenService.storeUserInfo(token, user);

            // 准备返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ResponseDTO.success("登录成功", data);
        } catch (Exception e) {
            log.error("用户登录异常", e);
            return ResponseDTO.error("登录异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO logout(String token) {
        try {
            // 使token失效
            tokenService.deleteToken(token);
            return ResponseDTO.success("退出成功");
        } catch (Exception e) {
            log.error("用户退出异常", e);
            return ResponseDTO.error("退出异常: " + e.getMessage());
        }
    }

    @Override
    public User getUserById(Integer userId) {
        return userMapper.selectUserById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAllUsers();
    }

    @Override
    public ResponseDTO updateUserStatus(Integer userId, Integer status) {
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        user.setUserStatus(status);
        int result = userMapper.updateUser(user);
        return result > 0 ? ResponseDTO.success("用户状态更新成功") : ResponseDTO.error("用户状态更新失败");
    }

    @Override
    public ResponseDTO updateUserInfo(User user) {
        int result = userMapper.updateUser(user);
        return result > 0 ? ResponseDTO.success("用户信息更新成功") : ResponseDTO.error("用户信息更新失败");
    }

    @Override
    public ResponseDTO deleteUser(Integer userId) {
        int result = userMapper.disableUser(userId); // 软删除
        return result > 0 ? ResponseDTO.success("用户删除成功") : ResponseDTO.error("用户删除失败");
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    public boolean validatePassword(String inputPassword, String encryptedPassword) {
        // 此方法签名缺少salt参数，不推荐使用
        return false;
    }

    @Override
    public boolean validatePassword(String inputPassword, String encryptedPassword, String salt) {
        return PasswordUtil.validatePassword(inputPassword, salt, encryptedPassword);
    }

    @Override
    public ResponseDTO updateUserPassword(Integer userId, String newPassword) {
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        
        String newSalt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encryptPassword(newPassword, newSalt);
        
        int result = userMapper.updateUserPassword(userId, encryptedPassword, newSalt);
        return result > 0 ? ResponseDTO.success("密码更新成功") : ResponseDTO.error("密码更新失败");
    }

    @Override
    public int getUserCount() {
        return userMapper.countUsers();
    }

    @Override
    public List<String> getRecentActivities() {
        // 这个方法需要关联行为记录表来实现
        return List.of();
    }

    @Override
    public UserStats getUserStats(Integer userId) {
        UserStats stats = new UserStats();
        stats.setUserId(userId);
        // 实际项目中应从数据库查询统计数据
        return stats;
    }

    @Override
    public ResponseDTO validateToken(String token) {
        try {
            // 验证JWT token有效性
            if (!jwtUtil.validateToken(token)) {
                return ResponseDTO.error("Token无效");
            }
            
            // 验证Redis中是否存在该token
            if (!tokenService.existsToken(token)) {
                return ResponseDTO.error("Token已过期");
            }
            
            // 获取用户信息
            User user = tokenService.getUserInfo(token);
            if (user == null) {
                return ResponseDTO.error("Token信息丢失");
            }
            
            return ResponseDTO.success("Token有效", user);
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return ResponseDTO.error("Token验证异常: " + e.getMessage());
        }
    }

    @Override
    public User getUserByToken(String token) {
        try {
            // 验证JWT token有效性
            if (!jwtUtil.validateToken(token)) {
                return null;
            }
            
            // 从Redis获取用户信息
            return tokenService.getUserInfo(token);
        } catch (Exception e) {
            log.error("通过Token获取用户信息异常", e);
            return null;
        }
    }

    @Override
    public ResponseDTO refreshToken(String token) {
        try {
            // 验证原token有效性
            if (!jwtUtil.validateToken(token) || !tokenService.existsToken(token)) {
                return ResponseDTO.error("原Token无效或已过期");
            }
            
            // 获取用户信息
            User user = tokenService.getUserInfo(token);
            if (user == null) {
                return ResponseDTO.error("用户信息丢失");
            }
            
            // 生成新token
            String newToken = jwtUtil.generateToken(user.getUserId(), user.getUsername());
            
            // 存储新token并删除旧token
            tokenService.storeUserInfo(newToken, user);
            tokenService.deleteToken(token);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            
            return ResponseDTO.success("Token刷新成功", data);
        } catch (Exception e) {
            log.error("Token刷新异常", e);
            return ResponseDTO.error("Token刷新异常: " + e.getMessage());
        }
    }

    @Override
    public String generateToken(Integer userId, String username) {
        return jwtUtil.generateToken(userId, username);
    }

    @Override
    public boolean invalidateToken(String token) {
        try {
            tokenService.deleteToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token失效异常", e);
            return false;
        }
    }

    @Override
    public long getTokenExpireTime(String token) {
        try {
            // 验证JWT token有效性
            if (!jwtUtil.validateToken(token)) {
                return -1;
            }
            
            // 获取剩余有效时间
            return jwtUtil.getTokenRemainingTime(token);
        } catch (Exception e) {
            log.error("获取Token过期时间异常", e);
            return -1;
        }
    }

    @Override
    public int cleanupExpiredTokens() {
        // 实际项目中应清理过期的token
        return 0;
    }

    @Override
    public ResponseDTO updateLastLoginTime(Integer userId) {
        int result = userMapper.updateLastLoginTime(userId, LocalDateTime.now());
        return result > 0 ? ResponseDTO.success("最后登录时间更新成功") : ResponseDTO.error("最后登录时间更新失败");
    }
}