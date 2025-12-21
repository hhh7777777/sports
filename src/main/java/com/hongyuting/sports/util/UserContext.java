package com.hongyuting.sports.util;

import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类，用于在同一个线程中存储和获取用户信息
 */
@Component
public class UserContext {

    private static final ThreadLocal<Integer> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public void setUserId(Integer userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public Integer getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前用户名
     */
    public void setUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取当前用户名
     */
    public String getUsername() {
        return USERNAME.get();
    }

    /**
     * 清除用户信息
     */
    public void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}