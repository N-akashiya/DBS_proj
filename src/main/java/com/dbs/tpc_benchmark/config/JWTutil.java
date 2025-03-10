package com.dbs.tpc_benchmark.config;

import com.dbs.tpc_benchmark.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;

@Component
public class JWTutil {
    private static Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 生成token
    public static String generateToken(User userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getName())
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
}
