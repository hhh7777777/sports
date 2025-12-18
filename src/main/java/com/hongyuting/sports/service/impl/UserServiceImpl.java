package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.entity.UserStats;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ResponseDTO register(RegisterDTO registerDTO) {
        try {
            // 检查用户名是否存在
            if (userMapper.existsByUsername(registerDTO.getUsername())) {
                return ResponseDTO.error("用户名已存在");
            }

            // 创建用户对象
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setNickname(registerDTO.getNickname());
            user.setEmail(registerDTO.getEmail());

            // 生成盐值并加密密码
            String salt = PasswordUtil.generateSalt();
            String encryptedPassword = PasswordUtil.encryptPassword(registerDTO.getPassword(), salt);

            user.setPassword(encryptedPassword);
            user.setSalt(salt);
            user.setUserStatus(1);
            user.setRegisterTime(LocalDateTime.now());
            user.setLastLoginTime(LocalDateTime.now());

            int result = userMapper.insertUser(user);
            return result > 0 ? ResponseDTO.success("注册成功") : ResponseDTO.error("注册失败");
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

            return ResponseDTO.success("登录成功");
        } catch (Exception e) {
            log.error("用户登录异常", e);
            return ResponseDTO.error("登录异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO logout(String token) {
        // 实际项目中应该使token失效
        return ResponseDTO.success("退出成功");
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
        // 实际项目中应验证JWT token
        return ResponseDTO.success("Token有效");
    }

    @Override
    public User getUserByToken(String token) {
        // 实际项目中应解析JWT token获取用户信息
        return null;
    }

    @Override
    public ResponseDTO refreshToken(String token) {
        // 实际项目中应生成新的JWT token
        return ResponseDTO.success("Token刷新成功");
    }

    @Override
    public String generateToken(Integer userId, String username) {
        // 实际项目中应生成JWT token
        return "fake_token_" + userId;
    }

    @Override
    public boolean invalidateToken(String token) {
        // 实际项目中应使JWT token失效
        return true;
    }

    @Override
    public long getTokenExpireTime(String token) {
        // 实际项目中应解析JWT token获取过期时间
        return 3600; // 默认1小时
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
