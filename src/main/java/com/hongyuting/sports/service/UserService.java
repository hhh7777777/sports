package com.hongyuting.sports.service;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 注册用户
     */
    ResponseDTO register(RegisterDTO registerDTO);
    /**
     * 登录用户
     */
    ResponseDTO login(LoginDTO loginDTO);
    /**
     * 登出用户
     */
    ResponseDTO logout(String token);
    /**
     * 验证Token有效性
     */
    ResponseDTO validateToken(String token);
    /**
     * 刷新Token
     */
    ResponseDTO refreshToken(String token);
    /**
     *  根据id获取用户信息
     */
    User getUserById(Integer userId);
    /**
     * 更新用户信息
     */
    ResponseDTO updateUserInfo(User user);
    /**
     * 更新用户头像
     */
    int updateUserAvatar(User user);
    /**
     * 更新用户状态
     */
    ResponseDTO updateUserStatus(Integer userId, Integer status);
    /**
     * 删除用户
     */

    ResponseDTO deleteUser(Integer userId);
    /**
     * 获取所有用户信息
     */
    List<User> getAllUsers();
    
    /**
     * 根据条件搜索用户
     */
    List<User> searchUsers(String username, String email, Integer status);
    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);
    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email);
    
    /**
     * 获取用户活跃度统计
     */
    Map<String, Object> getUserActivityStats(Integer userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户连续打卡天数
     */
    int getUserStreakDays(Integer userId);
    
    /**
     * 根据年月获取用户数量
     */
    int getUserCountByMonth(int year, int month);
}