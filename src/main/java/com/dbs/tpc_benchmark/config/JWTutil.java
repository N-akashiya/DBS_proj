package com.dbs.tpc_benchmark.config;

import com.dbs.tpc_benchmark.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.*;

@Component
public class JWTutil {
    // 密钥管理
    @Value("${jwt.secret}")
    private String secretKey;

    private Key SECRET_KEY;

    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 生成token
    public String generateToken(User userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getName())
                .claim("role", userDetails.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 有效期10小时
                .signWith(SECRET_KEY)
                .compact();
    }

    // 验证token（每次请求都会调用）
    public Boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Key getSecretKey() {
        return SECRET_KEY;
    }
}
