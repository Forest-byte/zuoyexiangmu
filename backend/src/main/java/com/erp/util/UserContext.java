package com.erp.util;

import com.erp.model.LoginUser;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) { HOLDER.set(user); }
    public static LoginUser get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    public static String currentName() {
        LoginUser u = HOLDER.get();
        return u == null ? "system" : u.getName();
    }

    public static String currentUsername() {
        LoginUser u = HOLDER.get();
        return u == null ? "system" : u.getUsername();
    }
}
