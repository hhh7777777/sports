package com.hongyuting.sports.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    @Setter
    @Getter
    private String confirmPassword;//确认密码
    private String salt;//密码盐
    private String email;
    private String nickname;
    private LocalDate birthday;
    private String gender;
    private Double height;
    private Double weight;
    private String avatar;
    private String wxOpenid;//微信openid
    private Integer userStatus = 1;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    @Setter
    @Getter
    private String captcha;//验证码

}