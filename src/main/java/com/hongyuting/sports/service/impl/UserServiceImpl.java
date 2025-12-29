package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.OperationLog;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.BadgeService;
import com.hongyuting.sports.service.OperationLogService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final SaltUtil saltUtil;
    
    private final BadgeService badgeService;
    private final TokenService tokenService;
    
    private final com.hongyuting.sports.mapper.BehaviorMapper behaviorMapper;
    private final com.hongyuting.sports.mapper.BadgeMapper badgeMapper;

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
            // 设置默认头像路径
            user.setAvatar("/images/avatar/avatar.png");

            // 生成盐值和加密密码
            String salt = saltUtil.generateSalt();
            String encryptedPassword = saltUtil.encryptPassword(registerDTO.getPassword(), salt);

            user.setPassword(encryptedPassword);
            user.setSalt(salt);
            user.setRegisterTime(LocalDateTime.now());

            // 插入用户
            int result = userMapper.insert(user);
            if (result > 0) {
                log.info("用户注册成功：用户ID={}", user.getUserId());
                
                // 自动检查徽章授予
                try {
                    ResponseDTO badgeResult = badgeService.autoGrantBadgesBasedOnBehavior(user.getUserId());
                    if (badgeResult.getCode() != 200) {
                        log.warn("自动授予徽章失败：用户ID={}, 错误={}", user.getUserId(), badgeResult.getMessage());
                    }
                } catch (Exception badgeException) {
                    log.error("自动授予徽章异常：用户ID={}", user.getUserId(), badgeException);
                }
                
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
        // 默认IP地址，用于向后兼容
        return login(loginDTO, "0:0:0:0:0:0:0:1");
    }

    @Override
    public ResponseDTO login(LoginDTO loginDTO, String clientIP) {
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
            User user = userMapper.selectByUsername(loginDTO.getUsername());
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
                userMapper.updateById(user);
                
                // 更新局部变量供后续使用
                encryptedPassword = newEncryptedPassword;
                salt = newSalt;
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUserId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

            // 将用户信息存入Redis (使用TokenService)
            tokenService.storeUserInfo(token, user);

            // 返回用户信息和Token
            user.setPassword(null); // 不返回密码
            user.setSalt(null); // 不返回盐值
            
            // 自动检查徽章授予
            try {
                ResponseDTO badgeResult = badgeService.autoGrantBadgesBasedOnBehavior(user.getUserId());
                if (badgeResult.getCode() != 200) {
                    log.warn("自动授予徽章失败：用户ID={}, 错误={}", user.getUserId(), badgeResult.getMessage());
                }
            } catch (Exception badgeException) {
                log.error("自动授予徽章异常：用户ID={}", user.getUserId(), badgeException);
            }

            log.info("用户登录成功：用户ID={}", user.getUserId());
            
            // 手动记录登录日志
            try {
                OperationLog loginLog = new OperationLog();
                loginLog.setUserId(user.getUserId());
                loginLog.setUserType("USER");
                loginLog.setOperation("用户登录");
                loginLog.setOperationType("AUTH");
                loginLog.setTargetType("SYSTEM");
                loginLog.setIpAddress(clientIP); // 使用传入的IP地址
                loginLog.setOperationTime(LocalDateTime.now());
                loginLog.setDetail("用户登录成功");
                
                operationLogService.addOperationLog(loginLog);
            } catch (Exception logException) {
                log.error("记录用户登录日志失败：用户ID={}", user.getUserId(), logException);
            }
            
            return ResponseDTO.success("登录成功", new LoginResponse(token, refreshToken, user));
        } catch (Exception e) {
            log.error("用户登录异常: ", e);
            return ResponseDTO.error("登录异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO logout(String token) {
        // 默认IP地址，用于向后兼容
        return logout(token, "0:0:0:0:0:0:0:1");
    }

    @Override
    public ResponseDTO logout(String token, String clientIP) {
        try {
            // 参数校验
            if (!StringUtils.hasText(token)) {
                return ResponseDTO.error("Token不能为空");
            }
            
            // 如果Token以"Bearer "开头，则去掉前缀
            String actualToken = token;
            if (token.startsWith("Bearer ")) {
                actualToken = token.substring(7);
            }
            
            // 验证JWT Token格式是否有效
            if (!jwtUtil.validateToken(actualToken)) {
                return ResponseDTO.error("Token格式无效");
            }
            
            // 从Redis中删除Token (使用TokenService)
            tokenService.deleteToken(actualToken);
            
            // 获取用户ID用于记录日志
            Integer userId = jwtUtil.getUserIdFromToken(actualToken);
            if (userId != null) {
                // 记录退出日志
                try {
                    OperationLog logoutLog = new OperationLog();
                    logoutLog.setUserId(userId);
                    logoutLog.setUserType("USER");
                    logoutLog.setOperation("用户退出");
                    logoutLog.setOperationType("AUTH");
                    logoutLog.setTargetType("SYSTEM");
                    logoutLog.setIpAddress(clientIP); // 使用传入的IP地址
                    logoutLog.setOperationTime(LocalDateTime.now());
                    logoutLog.setDetail("用户退出登录");
                    
                    operationLogService.addOperationLog(logoutLog);
                } catch (Exception logException) {
                    log.error("记录用户退出日志失败：用户ID={}", userId, logException);
                }
            }
            
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

            // 如果Token以"Bearer "开头，则去掉前缀
            String actualToken = token;
            if (token.startsWith("Bearer ")) {
                actualToken = token.substring(7);
            }

            // 首先验证JWT Token格式是否有效
            if (!jwtUtil.validateToken(actualToken)) {
                return ResponseDTO.error("Token格式无效");
            }

            // 从JWT Token中解析用户ID
            Integer userId = jwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return ResponseDTO.error("Token解析失败");
            }

            // 从Redis中获取用户信息
            User user = tokenService.getUserInfo(actualToken);
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

            // 如果Token以"Bearer "开头，则去掉前缀
            String actualToken = token;
            if (token.startsWith("Bearer ")) {
                actualToken = token.substring(7);
            }

            // 首先验证JWT Token格式是否有效
            if (!jwtUtil.validateToken(actualToken)) {
                return ResponseDTO.error("Token格式无效");
            }

            // 从Redis中获取用户信息 (使用TokenService)
            User user = tokenService.getUserInfo(actualToken);
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
            tokenService.deleteToken(actualToken);

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
            return userMapper.selectById(userId);
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
            User existingUser = userMapper.selectById(user.getUserId());
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
            
            // 更新用户状态（如果提供）
            if (user.getUserStatus() != null) {
                existingUser.setUserStatus(user.getUserStatus());
            }

            int result = userMapper.updateById(existingUser);
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
            return userMapper.updateUserAvatar(user.getUserId(), user.getAvatar());
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
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ResponseDTO.error("用户不存在");
            }
            
            // 更新用户状态
            user.setUserStatus(status);
            int result = userMapper.updateById(user);
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
            int result = userMapper.deleteById(userId);
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
            return userMapper.selectAll();
        } catch (Exception e) {
            log.error("获取所有用户异常: ", e);
            return null;
        }
    }

    @Override
    public List<User> searchUsers(String username, String email, Integer status) {
        try {
            return userMapper.searchUsers(username, email, status);
        } catch (Exception e) {
            log.error("搜索用户异常: ", e);
            return Collections.emptyList();
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

    @Override
    public Map<String, Object> getUserActivityStats(Integer userId, LocalDate startDate, LocalDate endDate) {
        try {
            if (userId == null || startDate == null || endDate == null) {
                return Collections.emptyMap();
            }
            
            // 获取用户在指定时间段内的总运动时长
            Integer totalDuration = userMapper.selectTotalDurationByUserAndDate(userId, startDate, endDate);
            if (totalDuration == null) {
                totalDuration = 0;
            }
            
            // 获取用户在指定时间段内的运动类型分布
            List<Map<String, Object>> typeDistribution = userMapper.selectBehaviorTypeDistribution(userId, startDate, endDate);
            
            // 构造返回结果
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDuration", totalDuration);
            stats.put("typeDistribution", typeDistribution);
            
            // 计算总次数
            int totalCount = 0;
            if (typeDistribution != null) {
                for (Map<String, Object> item : typeDistribution) {
                    // 获取每个类型的记录数
                    Object typeName = item.get("typeName");
                    if (typeName != null) {
                        // 通过关联查询获取每个类型的记录数
                        Integer recordCount = userMapper.countBehaviorRecordsByTypeAndDate(
                            userId, typeName.toString(), startDate, endDate);
                        item.put("recordCount", recordCount != null ? recordCount : 0);
                        totalCount += recordCount != null ? recordCount : 0;
                    }
                }
            }
            stats.put("totalCount", totalCount);
            
            return stats;
        } catch (Exception e) {
            log.error("获取用户活跃度统计异常: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public int getUserStreakDays(Integer userId) {
        try {
            if (userId == null) {
                return 0;
            }
            
            // 获取用户所有运动记录的日期
            List<LocalDate> exerciseDates = userMapper.selectExerciseDatesByUser(userId);
            
            if (exerciseDates == null || exerciseDates.isEmpty()) {
                return 0;
            }
            
            // 按日期排序（从新到旧）
            exerciseDates.sort(Collections.reverseOrder());
            
            // 计算连续天数
            int streakDays = 1;
            LocalDate currentDate = LocalDate.now();
            
            // 检查今天是否有运动记录
            if (!exerciseDates.contains(currentDate)) {
                // 如果今天没有运动记录，检查昨天是否有
                LocalDate yesterday = currentDate.minusDays(1);
                if (!exerciseDates.contains(yesterday)) {
                    // 如果昨天也没有运动记录，连续天数为0
                    return 0;
                } else {
                    // 如果昨天有运动记录，从昨天开始计算
                    currentDate = yesterday;
                }
            }
            
            // 向前检查连续天数
            for (int i = 1; i < exerciseDates.size(); i++) {
                LocalDate previousDate = currentDate.minusDays(1);
                if (exerciseDates.get(i).equals(previousDate)) {
                    streakDays++;
                    currentDate = previousDate;
                } else {
                    break;
                }
            }
            
            return streakDays;
        } catch (Exception e) {
            log.error("获取用户连续打卡天数异常: userId={}", userId, e);
            return 0;
        }
    }
    
    @Override
    public int getUserCountByMonth(int year, int month) {
        try {
            return userMapper.selectUserCountByMonth(year, month);
        } catch (Exception e) {
            log.error("获取月份用户数量异常: year={}, month={}", year, month, e);
            return 0;
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
            List<User> users = userMapper.selectAll();
            return users.stream()
                    .filter(user -> email.equals(user.getEmail()))
                    .anyMatch(user -> !userId.equals(user.getUserId()));
        } catch (Exception e) {
            log.error("检查邮箱是否被其他用户使用异常: userId={}, email={}", userId, email, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getUserPersonalStats(Integer userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户的总运动时长
            Integer totalDuration = userMapper.selectTotalDurationByUser(userId);
            stats.put("totalDuration", totalDuration != null ? totalDuration : 0);
            
            // 获取用户的总运动记录数
            int totalRecords = userMapper.selectCountByUserId(userId);
            stats.put("totalRecords", totalRecords);
            
            // 获取用户在活跃度排行中的位置
            List<Map<String, Object>> activityRank = behaviorMapper.selectActivityRank(Integer.MAX_VALUE);
            int myRank = -1;
            for (int i = 0; i < activityRank.size(); i++) {
                Map<String, Object> userRank = activityRank.get(i);
                if (userId.equals((Integer) userRank.get("userId"))) {
                    myRank = i + 1; // 排名从1开始
                    break;
                }
            }
            stats.put("myRank", myRank);
            
            // 获取总用户数
            int totalUserCount = userMapper.selectTotalCount();
            stats.put("totalUserCount", totalUserCount);
            
            return stats;
        } catch (Exception e) {
            log.error("获取用户个人统计信息异常，用户ID: {}", userId, e);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDuration", 0);
            stats.put("totalRecords", 0);
            stats.put("myRank", -1);
            stats.put("totalUserCount", 0);
            return stats;
        }
    }
    
    @Override
    public Map<String, Object> getUserRankInfo(Integer userId) {
        try {
            Map<String, Object> rankInfo = new HashMap<>();
            
            // 获取所有用户的活跃度排行
            List<Map<String, Object>> activityRank = behaviorMapper.selectActivityRank(Integer.MAX_VALUE);
            
            // 找到当前用户在排行中的位置
            int myRank = -1;
            for (int i = 0; i < activityRank.size(); i++) {
                Map<String, Object> userRank = activityRank.get(i);
                if (userId.equals((Integer) userRank.get("userId"))) {
                    myRank = i + 1; // 排名从1开始
                    break;
                }
            }
            
            // 获取总用户数
            int totalUserCount = userMapper.selectTotalCount();
            
            rankInfo.put("myRank", myRank);
            rankInfo.put("totalUserCount", totalUserCount);
            
            return rankInfo;
        } catch (Exception e) {
            log.error("获取用户排名信息异常，用户ID: {}", userId, e);
            Map<String, Object> rankInfo = new HashMap<>();
            rankInfo.put("myRank", -1);
            rankInfo.put("totalUserCount", 0);
            return rankInfo;
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
    
    // 添加获取客户端IP的辅助方法
    private String getClientIPFromLoginDTO(LoginDTO loginDTO) {
        // 由于LoginDTO中没有IP信息，我们返回一个默认值
        // 实际应用中应该从请求上下文中获取IP
        return "0:0:0:0:0:0:0:1"; // 默认IP，实际应用中应从请求上下文获取
    }
    
    @Override
    public List<Map<String, Object>> getUserGrowthStats(int months) {
        try {
            return userMapper.selectUserGrowthStats(months);
        } catch (Exception e) {
            log.error("获取用户增长趋势统计失败: ", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean hasUserClaimedDailyGiftToday(Integer userId) {
        return false;
    }

    @Override
    public boolean hasUserCompletedActivityToday(Integer userId) {
        return false;
    }

    @Override
    public boolean addUserPoints(Integer userId, int points) {
        return false;
    }

    @Override
    public boolean recordDailyGiftClaim(Integer userId) {
        return false;
    }

    private String getClientIPFromToken(String token) {
        // 由于Token中没有IP信息，我们返回一个默认值
        // 实际应用中应该从请求上下文获取IP
        return "0:0:0:0:0:0:0:1"; // 默认IP，实际应用中应从请求上下文获取
    }
    
    // 注入OperationLogService
    @Autowired
    private OperationLogService operationLogService;
}