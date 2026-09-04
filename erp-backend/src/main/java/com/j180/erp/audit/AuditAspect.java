package com.j180.erp.audit;

import com.j180.erp.common.util.JsonUtil;
import com.j180.erp.security.UserContext;
import com.j180.erp.security.UserContextHolder;
import com.j180.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 审计日志切面：拦截标注 @Audit 的接口方法，成功/失败均记录审计
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            record(audit, joinPoint.getArgs(), true, null);
            return result;
        } catch (Throwable e) {
            record(audit, joinPoint.getArgs(), false, e.getMessage());
            throw e;
        }
    }

    private void record(Audit audit, Object[] args, boolean success, String error) {
        try {
            UserContext context = UserContextHolder.get();
            Long userId = context != null ? context.getUserId() : null;
            String username = context != null ? context.getUsername() : null;
            // 变更后快照 = 请求参数（密码类字段脱敏）
            String afterSnapshot = JsonUtil.toMaskedJson(args != null && args.length == 1 ? args[0] : args);
            if (error != null) {
                afterSnapshot = appendError(afterSnapshot, error);
            }
            HttpServletRequest request = currentRequest();
            auditLogService.record(userId, username, audit.module(), audit.action(), audit.targetType(),
                    resolveTargetId(args), null, afterSnapshot, request);
        } catch (Exception e) {
            // 审计失败不影响主流程，但记录日志便于排查
            log.warn("审计日志记录失败: {}", e.getMessage());
        }
    }

    private String appendError(String snapshot, String error) {
        String base = snapshot == null ? "{}" : snapshot;
        return base.substring(0, base.length() - 1) + ",\"error\":\"" + error.replace("\"", "'").replace("\n", " ") + "\"}";
    }

    /**
     * 尽力提取操作对象ID：取参数中的 id / 路径变量
     */
    private Long resolveTargetId(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof Long id) {
                return id;
            }
            if (arg instanceof Number number) {
                return number.longValue();
            }
            try {
                Object id = arg.getClass().getMethod("getId").invoke(arg);
                if (id instanceof Number number) {
                    return number.longValue();
                }
            } catch (Exception ignored) {
                // 无 getId 方法，跳过
            }
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
