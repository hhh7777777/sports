package com.hongyuting.sports.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String email;
    private String nickname;
    private String password;
    private String confirmPassword;
    private String captcha;
}