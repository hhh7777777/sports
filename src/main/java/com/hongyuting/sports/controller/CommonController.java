package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.util.CaptchaUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
public class CommonController {

    @GetMapping("/api/common/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应类型
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成验证码
        String captchaText = CaptchaUtil.generateCaptcha();

        // 存储验证码到session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captchaText);
        session.setMaxInactiveInterval(300); // 5分钟过期

        // 创建验证码图片
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = image.createGraphics();

        // 设置背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 设置字体
        g2d.setFont(new Font("Arial", Font.BOLD, 24));

        // 绘制验证码
        for (int i = 0; i < captchaText.length(); i++) {
            g2d.setColor(new Color(
                    (int) (Math.random() * 128),
                    (int) (Math.random() * 128),
                    (int) (Math.random() * 128)
            ));
            g2d.drawString(String.valueOf(captchaText.charAt(i)), 20 + i * 20, 28);
        }

        // 添加干扰线
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = (int) (Math.random() * width);
            int y1 = (int) (Math.random() * height);
            int x2 = (int) (Math.random() * width);
            int y2 = (int) (Math.random() * height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();

        // 输出图片
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @GetMapping("/api/common/server-time")
    public ResponseDTO getServerTime() {
        return ResponseDTO.success("获取成功", System.currentTimeMillis());
    }
}