package com.j180.erp.security;

/**
 * 用户上下文持有器（ThreadLocal）
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        HOLDER.set(context);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static UserContext getRequired() {
        UserContext context = HOLDER.get();
        if (context == null) {
            throw new IllegalStateException("当前请求不存在用户上下文");
        }
        return context;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
