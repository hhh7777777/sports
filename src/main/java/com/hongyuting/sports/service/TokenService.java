package com.hongyuting.sports.service;

import com.alibaba.fastjson.JSON;
import com.hongyuting.sports.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * 存储用户信息到Redis
     */
    public void storeUserInfo(String token, User user) {
        String userJson = JSON.toJSONString(user);
        // 存储用户信息，过期时间与token一致
        redisTemplate.opsForValue().set(buildTokenKey(token), userJson, expiration, TimeUnit.MILLISECONDS);
        // 存储token与用户ID的映射
        redisTemplate.opsForValue().set(buildUserIdKey(user.getUserId()), token, expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * 从Redis获取用户信息
     */
    public User getUserInfo(String token) {
        String userJson = (String) redisTemplate.opsForValue().get(buildTokenKey(token));
        if (userJson != null) {
            return JSON.parseObject(userJson, User.class);
        }
        return null;
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
            String userJson = JSON.toJSONString(user);
            redisTemplate.expire(buildTokenKey(token), expiration, TimeUnit.MILLISECONDS);
            redisTemplate.expire(buildUserIdKey(user.getUserId()), expiration, TimeUnit.MILLISECONDS);
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