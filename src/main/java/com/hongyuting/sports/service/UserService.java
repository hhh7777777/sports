package com.hongyuting.sports.service;

import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.UserStats;

import java.util.List;

public interface UserService {

    ResponseDTO register(RegisterDTO registerDTO);

    ResponseDTO login(LoginDTO loginDTO);

    ResponseDTO logout(String token);

    User getUserById(Integer userId);

    User getUserByUsername(String username);

    List<User> getAllUsers();

    ResponseDTO updateUserStatus(Integer userId, Integer status);

    ResponseDTO updateUserInfo(User user);

    ResponseDTO deleteUser(Integer userId);

    boolean checkUsernameExists(String username);

    boolean checkEmailExists(String email);

    boolean validatePassword(String inputPassword, String encryptedPassword);

    /**
     * 验证密码
     */
    boolean validatePassword(String inputPassword, String encryptedPassword, String salt);

    /**
     * 更新用户密码
     */
    ResponseDTO updateUserPassword(Integer userId, String newPassword);

    // 新增方法：获取用户总数
    int getUserCount();

    List<String> getRecentActivities();

    // 新增方法：获取用户统计信息
    UserStats getUserStats(Integer userId);

    // ========== Token验证相关方法 ==========

    /**
     * 验证Token有效性
     *
     * @param token 用户令牌
     * @return 验证结果，true表示有效，false表示无效
     */
    ResponseDTO validateToken(String token);

    /**
     * 根据Token获取用户信息
     * @param token 用户令牌
     * @return 用户信息，如果Token无效返回null
     */
    User getUserByToken(String token);

    /**
     * 刷新Token
     *
     * @param token 旧令牌
     * @return 新的令牌，如果刷新失败返回null
     */
    ResponseDTO refreshToken(String token);

    /**
     * 生成新的Token
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的Token字符串
     */
    String generateToken(Integer userId, String username);

    /**
     * 使Token失效
     * @param token 需要失效的令牌
     * @return 操作结果，true表示成功，false表示失败
     */
    boolean invalidateToken(String token);

    /**
     * 检查Token是否即将过期
     * @param token 用户令牌
     * @return 剩余有效时间（分钟），如果Token无效返回-1
     */
    long getTokenExpireTime(String token);

    /**
     * 清理过期的Token
     * @return 清理的Token数量
     */
    int cleanupExpiredTokens();

    // 新增方法：更新用户最后登录时间
    ResponseDTO updateLastLoginTime(Integer userId);
}