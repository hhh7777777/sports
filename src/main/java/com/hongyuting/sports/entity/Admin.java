package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;
/**
 * 管理员实体类
 */
@Data
public class Admin {
    private Integer adminId;
    private String username;
    private String password;
    private String salt;
    private String email;
    private Integer roleLevel;
    private String department;
    private Integer status; // 1-启用, 0-禁用
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginTime;
}