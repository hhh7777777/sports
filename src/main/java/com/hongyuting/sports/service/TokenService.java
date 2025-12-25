package com.hongyuting.sports.service;

import com.hongyuting.sports.entity.User;
/**
 * Token服务接口
 */
public interface TokenService {

    /**
     * 存储用户信息到Redis
     */
    void storeUserInfo(String token, User user);

    /**
     * 从Redis获取用户信息
     */
    User getUserInfo(String token);

    /**
     * 删除token和用户信息
     */
    void deleteToken(String token);

    /**
     * 根据用户ID删除token
     */
    void deleteTokenByUserId(Integer userId);

    /**
     * 刷新token过期时间
     */
    void refreshToken(String token);

    /**
     * 检查token是否存在
     */
    boolean existsToken(String token);

    /**
     * 存储管理员信息到Redis
     */
    void storeAdminInfo(String token, Object admin);

    /**
     * 从Redis获取管理员信息
     */
    Object getAdminInfo(String token);

    /**
     * 删除管理员token和信息
     */
    void deleteAdminToken(String token);

    /**
     * 根据管理员ID删除token
     */
    void deleteAdminTokenByUserId(Integer adminId);

    /**
     * 刷新管理员token过期时间
     */
    void refreshAdminToken(String token);

    /**
     * 检查管理员token是否存在
     */
    boolean existsAdminToken(String token);
}