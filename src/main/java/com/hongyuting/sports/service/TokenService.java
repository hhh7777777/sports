package com.hongyuting.sports.service;

import com.hongyuting.sports.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token服务类
 */
@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Token过期时间（毫秒）- 24小时
    private static final long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000L;
    
    // Refresh Token过期时间（毫秒）- 30天
    private static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L;

    /**
     * 存储用户信息到Redis
     */
    public void storeUserInfo(String token, User user) {
        // 存储用户信息，过期时间与token一致
        redisTemplate.opsForValue().set(buildTokenKey(token), user, TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        // 存储token与用户ID的映射
        redisTemplate.opsForValue().set(buildUserIdKey(user.getUserId()), token, TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    /**
     * 从Redis获取用户信息
     */
    public User getUserInfo(String token) {
        return (User) redisTemplate.opsForValue().get(buildTokenKey(token));
    }

    /**
     * 删除token和用户信息
     */
    public void deleteToken(String token) {
        User user = getUserInfo(token);
        if (user != null) {
            redisTemplate.delete(buildTokenKey(token));
            redisTemplate.delete(buildUserIdKey(user.getUserId()));
        }
    }

    /**
     * 根据用户ID删除token
     */
    public void deleteTokenByUserId(Integer userId) {
        String token = (String) redisTemplate.opsForValue().get(buildUserIdKey(userId));
        if (token != null) {
            deleteToken(token);
        }
    }

    /**
     * 刷新token过期时间
     */
    public void refreshToken(String token) {
        User user = getUserInfo(token);
        if (user != null) {
            redisTemplate.expire(buildTokenKey(token), TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
            redisTemplate.expire(buildUserIdKey(user.getUserId()), TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查token是否存在
     */
    public boolean existsToken(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildTokenKey(token)));
    }

    private String buildTokenKey(String token) {
        return "user:token:" + token;
    }

    private String buildUserIdKey(Integer userId) {
        return "user:id:" + userId;
    }
}