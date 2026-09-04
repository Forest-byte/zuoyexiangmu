package com.erp.util;

import com.erp.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具：生成/解析 token
 */
@Component
public class JwtUtil {

    @Value("${erp.jwt.secret}")
    private String secret;

    @Value("${erp.jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(LoginUser user) {
        Map<String, Object> claims = Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "name", user.getName() == null ? "" : user.getName(),
                "roleCode", user.getRoleCode() == null ? "" : user.getRoleCode(),
                "isAdmin", user.getIsAdmin() != null && user.getIsAdmin()
        );
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireHours * 3600_000L))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public LoginUser parse(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        LoginUser u = new LoginUser();
        Object uid = claims.get("userId");
        u.setUserId(uid == null ? null : Long.valueOf(String.valueOf(uid)));
        u.setUsername(claims.get("username", String.class));
        u.setName(claims.get("name", String.class));
        u.setRoleCode(claims.get("roleCode", String.class));
        Object adminObj = claims.get("isAdmin");
        u.setIsAdmin(adminObj != null && Boolean.parseBoolean(String.valueOf(adminObj)));
        return u;
    }
}
