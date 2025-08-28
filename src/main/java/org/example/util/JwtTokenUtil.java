package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenUtil {
    // 应用密钥 - 至少32个字符（256位）的安全密钥
    public static final String APP_SECRET_KEY = "secure-jwt-signing-key-with-1234567890-chars";

    // 生成密钥
    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            APP_SECRET_KEY.getBytes(StandardCharsets.UTF_8)
    );

    /**
     * 生成Token
     */
    public static String createToken(String username) {
        return Jwts.builder()
                .subject("app")
                .claim("userId", username)
                .issuedAt(new Date())
                .signWith(KEY)
                .compact();
    }

    /**
     * 校验Token并返回Claims
     */
    public static boolean checkJWT(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取负载
     */
    public static Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /**
     * 从Token中获取username
     */
    public static String getUserId(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("userId", String.class) : null;
    }

}
