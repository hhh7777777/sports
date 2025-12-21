package com.hongyuting.sports.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForHongYuTingSportsApplication2024}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        // 确保密钥长度足够
        if (secret.length() < 32) {
            // 如果密钥太短，进行填充
            StringBuilder builder = new StringBuilder(secret);
            while (builder.length() < 32) {
                builder.append("0");
            }
            secret = builder.substring(0, 32);
        } else if (secret.length() > 32) {
            secret = secret.substring(0, 32);
        }
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成Token
     */
    public String generateToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000); // 24小时

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000); // 30天

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * 获取Token的过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 从Token中解析Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查Token是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, long minutes) {
        Date expiration = getExpirationDateFromToken(token);
        long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
        return timeUntilExpiration < (minutes * 60 * 1000);
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return -1;
        }
    }
}