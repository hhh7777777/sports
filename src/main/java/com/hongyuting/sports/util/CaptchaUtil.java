package com.hongyuting.sports.util;

import java.util.Random;

public class CaptchaUtil {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * 生成随机验证码
     */
    public static String generateCaptcha() {
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
    public static String generateNumericCaptcha(int length) {
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
    public static boolean validateCaptcha(String input, String captcha) {
        if (input == null || captcha == null) {
            return false;
        }
        return input.equalsIgnoreCase(captcha);
    }
}