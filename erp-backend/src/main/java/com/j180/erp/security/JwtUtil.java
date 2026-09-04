package com.j180.erp.security;

import com.j180.erp.common.BizException;
import com.j180.erp.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具：载荷仅含 userId + roleIds（最小化载荷，权限实时从服务端获取）
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${erp.jwt.secret}")
    private String secret;

    @Value("${erp.jwt.expire-hours:8}")
    private long expireHours;

    private byte[] secretKeyBytes;

    @PostConstruct
    public void init() {
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 签发 Token
     */
    public String generateToken(Long userId, List<Long> roleIds) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("userId", userId);
        claims.put("roleIds", roleIds);
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(Keys.hmacShaKeyFor(secretKeyBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token，无效/过期返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKeyBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.debug("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中取用户ID（解析失败抛 401 业务异常）
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            throw new BizException(Result.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        Number userId = claims.get("userId", Number.class);
        if (userId == null) {
            throw new BizException(Result.UNAUTHORIZED, "非法Token");
        }
        return userId.longValue();
    }
}
