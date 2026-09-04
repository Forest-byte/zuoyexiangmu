package com.j180.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j180.erp.common.Result;
import com.j180.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 鉴权拦截器：
 * ① 解析 JWT 认证 -> ② 实时构建用户上下文（角色/功能权限/数据权限） ->
 * ③ 校验 @RequiresPermission 声明的功能权限，越权返回 403 并写审计日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.fail(Result.UNAUTHORIZED, "未登录或登录已过期"));
            return false;
        }
        UserContext context;
        try {
            Long userId = jwtUtil.getUserId(token);
            context = permissionService.buildContext(userId);
        } catch (com.j180.erp.common.BizException e) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.fail(Result.UNAUTHORIZED, e.getMessage()));
            return false;
        } catch (Exception e) {
            log.error("构建用户上下文失败", e);
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.fail(Result.UNAUTHORIZED, "登录状态无效，请重新登录"));
            return false;
        }

        // 功能权限校验（服务端强制校验 R-MODEL-7）
        if (handler instanceof HandlerMethod) {
            RequiresPermission annotation = ((HandlerMethod) handler).getMethodAnnotation(RequiresPermission.class);
            if (annotation != null && !context.hasPermission(annotation.value())) {
                // 越权访问写入审计日志（AC-RP-02 / AC-RP-10）
                auditLogService.record(context.getUserId(), context.getUsername(), "权限中心",
                        "越权访问", "接口", null, null,
                        "尝试访问无权限接口: " + request.getMethod() + " " + request.getRequestURI(), request);
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.fail(Result.FORBIDDEN, "无权限访问"));
                return false;
            }
        }
        UserContextHolder.set(context);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> result) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
