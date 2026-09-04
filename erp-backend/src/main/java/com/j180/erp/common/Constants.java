package com.j180.erp.common;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {
    }

    /** 内置超级管理员角色编码 */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    /** 内置超级管理员账号 */
    public static final String SUPER_ADMIN_USER = "admin";

    /** 状态：停用/启用 */
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    /** 员工状态：0=试用 1=在职 2=离职 */
    public static final int EMP_TRIAL = 0;
    public static final int EMP_ON_JOB = 1;
    public static final int EMP_LEAVED = 2;

    /** 资源类型 */
    public static final int RES_MENU = 1;
    public static final int RES_PAGE = 2;
    public static final int RES_BUTTON = 3;
    public static final int RES_API = 4;

    /** 密码策略：至少8位且同时包含字母与数字 */
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,32}$";
    public static final String PASSWORD_TIP = "密码长度至少8位，且必须同时包含字母和数字";
}
