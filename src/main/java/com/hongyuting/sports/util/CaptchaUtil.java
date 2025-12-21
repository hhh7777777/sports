package com.hongyuting.sports.util;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码工具类
 */
@Component
public class CaptchaUtil {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * 生成随机验证码
     */
    public String generateCaptcha() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();

        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            captcha.append(CHARACTERS.charAt(index));
        }

        return captcha.toString();
    }

    /**
     * 生成数字验证码
     */
    public String generateNumericCaptcha(int length) {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();

        for (int i = 0; i < length; i++) {
            captcha.append(random.nextInt(10));
        }

        return captcha.toString();
    }

    /**
     * 验证验证码（不区分大小写）
     */
    public boolean validateCaptcha(String input, String captcha) {
        if (input == null || captcha == null) {
            return false;
        }
        return input.equalsIgnoreCase(captcha);
    }

    /**
     * 生成验证码图片
     *
     * @param captchaText 验证码文本
     * @param width 图片宽度
     * @param height 图片高度
     * @return 验证码图片
     */
    public BufferedImage generateCaptchaImage(String captchaText, int width, int height) {
        // 创建验证码图片
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
        return image;
    }

    /**
     * 生成默认尺寸的验证码图片(120x40)
     *
     * @param captchaText 验证码文本
     * @return 验证码图片
     */
    public BufferedImage generateCaptchaImage(String captchaText) {
        return generateCaptchaImage(captchaText, 120, 40);
    }
}