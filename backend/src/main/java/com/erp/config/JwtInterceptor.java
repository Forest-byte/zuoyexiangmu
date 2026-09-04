package com.erp.config;

import com.erp.annotation.RequirePermission;
import com.erp.common.BusinessException;
import com.erp.model.LoginUser;
import com.erp.service.AuthService;
import com.erp.util.JwtUtil;
import com.erp.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证 + RBAC 权限拦截器
 * 未登录 -> 401；越权 -> 403；admin 全权限
 * 权限码按 userId 每次从数据库动态加载（token 不携带权限，避免超大 header）
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public JwtInterceptor(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 放行登录/登出
        String uri = request.getRequestURI();
        if (uri.endsWith("/api/auth/login") || uri.endsWith("/api/auth/logout")) {
            return true;
        }
        // 放行静态与健康检查
        if (uri.equals("/") || uri.equals("/error") || uri.equals("/favicon.ico")) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            write(response, 401, "未登录或登录已过期");
            return false;
        }
        String token = auth.substring(7);
        LoginUser user;
        try {
            user = jwtUtil.parse(token);
        } catch (Exception e) {
            write(response, 401, "登录状态无效，请重新登录");
            return false;
        }
        UserContext.set(user);

        // 权限校验（权限码从数据库动态加载）
        if (handler instanceof HandlerMethod hm) {
            RequirePermission rp = hm.getMethodAnnotation(RequirePermission.class);
            if (rp != null) {
                boolean admin = user.getIsAdmin() != null && user.getIsAdmin();
                if (!admin) {
                    java.util.Set<String> perms = authService.permissionsOf(user.getUserId());
                    boolean has = perms != null && perms.contains(rp.value());
                    if (!has) {
                        UserContext.clear();
                        write(response, 403, "无权限执行该操作：" + rp.value());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void write(HttpServletResponse response, int code, String msg) {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + msg + "\",\"data\":null}");
        } catch (Exception ignored) {
        }
    }
}
