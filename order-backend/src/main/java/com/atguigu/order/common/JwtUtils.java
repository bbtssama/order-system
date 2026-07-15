package com.atguigu.order.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 负责 JWT Token 的生成、解析和验证。
 * 密钥和有效期通过 application.properties 配置，支持环境变量覆盖。
 * Token 中携带 userId、username、role 三个声明。
 * </p>
 */
@Component
public class JwtUtils {

    /** 签名密钥（从配置读取，最少 32 字节以满足 HS256 算法要求） */
    @Value("${jwt.secret}")
    private String secret;

    /** Token 有效期（毫秒） */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 构建 HMAC-SHA 签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（USER / ADMIN）
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token 获取 Claims
     *
     * @param token JWT Token
     * @return Claims 声明集合
     * @throws io.jsonwebtoken.JwtException Token 无效或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否有效（签名正确且未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 从 Token 中提取用户ID */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /** 从 Token 中提取用户名 */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /** 从 Token 中提取角色 */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}
