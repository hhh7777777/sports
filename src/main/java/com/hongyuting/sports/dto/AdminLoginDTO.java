package com.hongyuting.sports.dto;

import lombok.Data;
/**
 * 管理员登录传输对象
 */
@Data
public class AdminLoginDTO {
    private String username;
    private String password;
    private String captcha;
}