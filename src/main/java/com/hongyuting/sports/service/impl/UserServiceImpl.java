package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.TokenService;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.util.JwtUtil;
import com.hongyuting.sports.util.SaltUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final SaltUtil saltUtil;

    @Autowired
    private TokenService tokenService;

    @Override
    public ResponseDTO register(RegisterDTO registerDTO) {
        try {
            // 参数校验
            if (registerDTO == null) {
                return ResponseDTO.error("注册信息不能为空");
            }
            
            if (!StringUtils.hasText(registerDTO.getUsername())) {
                return ResponseDTO.error("用户名不能为空");
            }
            
            if (!StringUtils.hasText(registerDTO.getPassword())) {
                return ResponseDTO.error("密码不能为空");
            }
            
            if (!StringUtils.hasText(registerDTO.getEmail())) {
                return ResponseDTO.error("邮箱不能为空");
            }
            
            if (registerDTO.getPassword().length() < 6) {
                return ResponseDTO.error("密码长度不能少于6位");
            }

            // 检查用户名是否已存在
            if (checkUsernameExists(registerDTO.getUsername())) {
                return ResponseDTO.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (checkEmailExists(registerDTO.getEmail())) {
                return ResponseDTO.error("邮箱已被注册");
            }

            // 创建用户对象
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setEmail(registerDTO.getEmail());
            user.setNickname(registerDTO.getNickname());

            // 生成盐值和加密密码
            String salt = saltUtil.generateSalt();
            String encryptedPassword = saltUtil.encryptPassword(registerDTO.getPassword(), salt);

            user.setPassword(encryptedPassword);
            user.setSalt(salt);
            user.setRegisterTime(LocalDateTime.now());

            // 插入用户
            int result = userMapper.insertUser(user);
            if (result > 0) {
                log.info("用户注册成功：用户ID={}", user.getUserId());
                return ResponseDTO.success("注册成功");
            } else {
                log.warn("用户注册失败：用户名={}", registerDTO.getUsername());
                return ResponseDTO.error("注册失败");
            }
        } catch (Exception e) {
            log.error("用户注册异常: ", e);
            return ResponseDTO.error("注册异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO login(LoginDTO loginDTO) {
        try {
            // 参数校验
            if (loginDTO == null) {
                return ResponseDTO.error("登录信息不能为空");
            }
            
            if (!StringUtils.hasText(loginDTO.getUsername())) {
                return ResponseDTO.error("用户名不能为空");
            }
            
            if (!StringUtils.hasText(loginDTO.getPassword())) {
                return ResponseDTO.error("密码不能为空");
            }

            // 根据用户名查找用户
            User user = userMapper.selectUserByUsername(loginDTO.getUsername());
            if (user == null) {
                log.warn("用户登录失败：用户名或密码错误，用户名={}", loginDTO.getUsername());
                return ResponseDTO.error("用户名或密码错误");
            }

            // 验证密码
            boolean passwordValid = false;
            boolean needToUpdatePassword = false;
            String encryptedPassword = user.getPassword();
            String salt = user.getSalt();
            
            if (user.getSalt() == null || user.getSalt().isEmpty()) {
                // 明文密码比较（兼容旧账户）
                passwordValid = loginDTO.getPassword().equals(user.getPassword());
                // 如果验证通过且是明文密码，标记需要更新为加密密码
                if (passwordValid) {
                    needToUpdatePassword = true;
                }
            } else {
                // 加盐加密验证
                passwordValid = saltUtil.verifyPassword(loginDTO.getPassword(), user.getPassword(), user.getSalt());
            }

            if (!passwordValid) {
                log.warn("用户登录失败：用户名或密码错误，用户ID={}", user.getUserId());
                return ResponseDTO.error("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getUserStatus() != null && user.getUserStatus() != 1) {
                log.warn("用户登录失败：账户已被禁用，用户ID={}", user.getUserId());
                return ResponseDTO.error("账户已被禁用");
            }

            // 如果是旧账户（明文存储），则升级为加密存储
            if (needToUpdatePassword) {
                // 生成盐值
                String newSalt = saltUtil.generateSalt();
                // 加密密码
                String newEncryptedPassword = saltUtil.encryptPassword(loginDTO.getPassword(), newSalt);
                
                // 更新用户信息
                user.setPassword(newEncryptedPassword);
                user.setSalt(newSalt);
                userMapper.updateUser(user);
                
                // 更新局部变量供后续使用
                encryptedPassword = newEncryptedPassword;
                salt = newSalt;
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateUser(user);

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUserId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            // 将用户信息存入Redis (使用TokenService)
            tokenService.storeUserInfo(token, user);

            // 返回用户信息和Token
            user.setPassword(null); // 不返回密码
            user.setSalt(null); // 不返回盐值

            log.info("用户登录成功：用户ID={}", user.getUserId());
            return ResponseDTO.success("登录成功", new LoginResponse(token, refreshToken, user));
        } catch (Exception e) {
            log.error("用户登录异常: ", e);
            return ResponseDTO.error("登录异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO logout(String token) {
        try {
            // 参数校验
            if (!StringUtils.hasText(token)) {
                return ResponseDTO.error("Token不能为空");
            }
            
            // 从Redis中删除Token (使用TokenService)
            tokenService.deleteToken(token.replace("Bearer ", ""));
            log.info("用户退出登录成功");
            return ResponseDTO.success("退出成功");
        } catch (Exception e) {
            log.error("用户退出异常: ", e);
            return ResponseDTO.error("退出异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO validateToken(String token) {
        try {
            // 参数校验
            if (!StringUtils.hasText(token)) {
                return ResponseDTO.error("Token不能为空");
            }

            // 从Redis中获取用户信息 (使用TokenService)
            User user = tokenService.getUserInfo(token);
            if (user == null) {
                return ResponseDTO.error("Token无效或已过期");
            }

            // 检查用户状态
            if (user.getUserStatus() != null && user.getUserStatus() != 1) {
                return ResponseDTO.error("账户已被禁用");
            }

            user.setPassword(null); // 不返回密码
            user.setSalt(null); // 不返回盐值
            
            return ResponseDTO.success("Token有效", user);
        } catch (Exception e) {
            log.error("Token验证异常: ", e);
            return ResponseDTO.error("Token验证异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO refreshToken(String token) {
        try {
            // 参数校验
            if (!StringUtils.hasText(token)) {
                return ResponseDTO.error("Token不能为空");
            }

            // 从Redis中获取用户信息 (使用TokenService)
            User user = tokenService.getUserInfo(token);
            if (user == null) {
                return ResponseDTO.error("Token无效或已过期");
            }

            // 检查用户状态
            if (user.getUserStatus() != null && user.getUserStatus() != 1) {
                return ResponseDTO.error("账户已被禁用");
            }

            // 生成新的Token
            String newToken = jwtUtil.generateToken(user.getUserId());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            // 存储新的Token并删除旧的Token (使用TokenService)
            tokenService.storeUserInfo(newToken, user);
            tokenService.deleteToken(token);

            // 构造返回结果
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(newToken);
            loginResponse.setRefreshToken(newRefreshToken);
            loginResponse.setUser(user);

            log.info("用户刷新Token成功：用户ID={}", user.getUserId());
            return ResponseDTO.success("刷新Token成功", loginResponse);
        } catch (Exception e) {
            log.error("刷新Token异常: ", e);
            return ResponseDTO.error("刷新Token异常: " + e.getMessage());
        }
    }

    @Override
    public User getUserById(Integer userId) {
        try {
            if (userId == null) {
                return null;
            }
            return userMapper.selectUserById(userId);
        } catch (Exception e) {
            log.error("根据ID获取用户信息异常: userId={}", userId, e);
            return null;
        }
    }

    @Override
    public ResponseDTO updateUserInfo(User user) {
        try {
            // 参数校验
            if (user == null || user.getUserId() == null) {
                return ResponseDTO.error("用户信息不能为空");
            }

            // 检查邮箱是否已被其他用户使用
            if (StringUtils.hasText(user.getEmail()) && checkEmailExistsForOtherUser(user.getUserId(), user.getEmail())) {
                return ResponseDTO.error("邮箱已被其他用户使用");
            }

            // 更新用户信息
            User existingUser = userMapper.selectUserById(user.getUserId());
            if (existingUser == null) {
                return ResponseDTO.error("用户不存在");
            }

            // 只更新非空字段
            if (StringUtils.hasText(user.getNickname())) {
                existingUser.setNickname(user.getNickname());
            }
            if (StringUtils.hasText(user.getEmail())) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getBirthday() != null) {
                existingUser.setBirthday(user.getBirthday());
            }
            if (StringUtils.hasText(user.getGender())) {
                existingUser.setGender(user.getGender());
            }
            if (user.getHeight() != null) {
                existingUser.setHeight(user.getHeight());
            }
            if (user.getWeight() != null) {
                existingUser.setWeight(user.getWeight());
            }
            
            // 更新用户头像（如果有）
            if (StringUtils.hasText(user.getAvatar())) {
                existingUser.setAvatar(user.getAvatar());
            }

            int result = userMapper.updateUser(existingUser);
            if (result > 0) {
                log.info("更新用户信息成功：用户ID={}", user.getUserId());
                // 清除Redis中的用户信息缓存
                String tokenKeyPattern = "user:token:*";
                redisTemplate.delete(redisTemplate.keys(tokenKeyPattern));
                return ResponseDTO.success("用户信息更新成功");
            } else {
                log.warn("更新用户信息失败：用户ID={}", user.getUserId());
                return ResponseDTO.error("用户信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息异常: userId={}", user != null ? user.getUserId() : null, e);
            return ResponseDTO.error("用户信息更新异常: " + e.getMessage());
        }
    }

    @Override
    public int updateUserAvatar(User user) {
        try {
            // 更新用户头像
            if (user == null || user.getUserId() == null) {
                return 0;
            }
            return userMapper.updateUserAvatar(user);
        } catch (Exception e) {
            log.error("更新用户头像异常: userId={}", user != null ? user.getUserId() : null, e);
            return 0;
        }
    }

    @Override
    public ResponseDTO updateUserStatus(Integer userId, Integer status) {
        try {
            // 参数校验
            if (userId == null) {
                return ResponseDTO.error("用户ID不能为空");
            }
            
            if (status == null) {
                return ResponseDTO.error("状态不能为空");
            }

            // 先获取用户信息
            User user = userMapper.selectUserById(userId);
            if (user == null) {
                return ResponseDTO.error("用户不存在");
            }
            
            // 更新用户状态
            user.setUserStatus(status);
            int result = userMapper.updateUser(user);
            if (result > 0) {
                log.info("更新用户状态成功：用户ID={}，状态={}", userId, status);
                return ResponseDTO.success("用户状态更新成功");
            } else {
                log.warn("更新用户状态失败：用户ID={}，状态={}", userId, status);
                return ResponseDTO.error("用户状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态异常: userId={}, status={}", userId, status, e);
            return ResponseDTO.error("用户状态更新异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO deleteUser(Integer userId) {
        try {
            // 参数校验
            if (userId == null) {
                return ResponseDTO.error("用户ID不能为空");
            }

            // 删除用户
            int result = userMapper.deleteUser(userId);
            if (result > 0) {
                log.info("删除用户成功：用户ID={}", userId);
                return ResponseDTO.success("用户删除成功");
            } else {
                log.warn("删除用户失败：用户ID={}", userId);
                return ResponseDTO.error("用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户异常: userId={}", userId, e);
            return ResponseDTO.error("用户删除异常: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            return userMapper.selectAllUsers();
        } catch (Exception e) {
            log.error("获取所有用户异常: ", e);
            return null;
        }
    }

    @Override
    public boolean checkUsernameExists(String username) {
        try {
            if (!StringUtils.hasText(username)) {
                return false;
            }
            return userMapper.countByUsername(username) > 0;
        } catch (Exception e) {
            log.error("检查用户名是否存在异常: username={}", username, e);
            return false;
        }
    }

    @Override
    public boolean checkEmailExists(String email) {
        try {
            if (!StringUtils.hasText(email)) {
                return false;
            }
            return userMapper.countByEmail(email) > 0;
        } catch (Exception e) {
            log.error("检查邮箱是否存在异常: email={}", email, e);
            return false;
        }
    }

    /**
     * 检查邮箱是否被其他用户使用
     */
    private boolean checkEmailExistsForOtherUser(Integer userId, String email) {
        try {
            if (userId == null || !StringUtils.hasText(email)) {
                return false;
            }
            
            // 先获取所有具有该邮箱的用户
            List<User> users = userMapper.selectAllUsers();
            return users.stream()
                    .filter(user -> email.equals(user.getEmail()))
                    .anyMatch(user -> !userId.equals(user.getUserId()));
        } catch (Exception e) {
            log.error("检查邮箱是否被其他用户使用异常: userId={}, email={}", userId, email, e);
            return false;
        }
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String refreshToken;
        private User user;

        public LoginResponse() {}

        public LoginResponse(String token, String refreshToken, User user) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }
}