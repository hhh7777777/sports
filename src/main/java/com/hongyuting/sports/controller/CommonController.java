package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.util.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 通用接口
 */
@RestController
public class CommonController {
    
    @Autowired
    private CaptchaUtil captchaUtil;

    //验证码拦截器
    @GetMapping("/api/common/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应类型
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成验证码
        String captchaText = captchaUtil.generateCaptcha();

        // 存储验证码到session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captchaText);
        session.setMaxInactiveInterval(300); // 5分钟过期

        // 创建验证码图片
        BufferedImage image = captchaUtil.generateCaptchaImage(captchaText);

        // 输出图片
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @GetMapping("/api/common/server-time")
    public ResponseDTO getServerTime() {
        return ResponseDTO.success("获取成功", System.currentTimeMillis());
    }
}