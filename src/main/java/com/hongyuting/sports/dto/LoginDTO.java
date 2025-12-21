package com.hongyuting.sports.dto;

import lombok.Data;
/**
 * 管理员登录DTO
 */
@Data
public class LoginDTO {
    private String username;
    private String password;
    private String captcha;
}
