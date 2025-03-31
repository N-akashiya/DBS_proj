package com.dbs.tpc_benchmark.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTinterceptor implements HandlerInterceptor {
    @Autowired
    private JWTutil jwtutil;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String path = request.getRequestURI();
        System.out.println("JWTinterceptor: Processing request to " + request.getRequestURI());
        
        // 检查路径
        if (path.contains("/error") || path.equals("/users/login") || path.equals("/users/register"))
            return true; 
        
        // 获取Authorization header
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("JWTinterceptor: Missing or invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        // 去掉Bearer
        token = token.substring(7);
        try {
            // 验证
            Jwts.parserBuilder().setSigningKey(jwtutil.getSecretKey()).build().parseClaimsJws(token);
            // 解析
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtutil.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 检查角色
            String role = claims.get("role", String.class);
            request.setAttribute("role", role);
            if (path.startsWith("/admin/") && !"ADMIN".equals(role)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient privileges");
                return false;
            }
            return true;
        } catch (SignatureException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
            return false;
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT security exception");
            return false;
        } catch (ExpiredJwtException e) {
            response.setHeader("X-Token-Expired", "true");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired JWT token");
            return false;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return false;
        }
    
    }

}