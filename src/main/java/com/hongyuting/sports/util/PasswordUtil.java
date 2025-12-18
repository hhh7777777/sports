//PasswordUtil 支持加盐加密
package com.hongyuting.sports.util;

import org.springframework.util.DigestUtils;
import java.util.UUID;

public class PasswordUtil {

    // 默认盐值长度
    private static final int SALT_LENGTH = 8;

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, SALT_LENGTH);
    }

    /**
     * 对密码进行加盐MD5加密
     */
    public static String encryptPassword(String password, String salt) {
        String saltedPassword = password + salt;
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes());
    }

    /**
     * 验证密码（加盐版本）
     */
    public static boolean validatePassword(String inputPassword, String salt, String encryptedPassword) {
        if (inputPassword == null || salt == null || encryptedPassword == null) {
            return false;
        }
        String encryptedInput = encryptPassword(inputPassword, salt);
        return encryptedInput.equals(encryptedPassword);
    }

    /**
     * 生成随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.util.Random random = new java.util.Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * 检查密码强度
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}