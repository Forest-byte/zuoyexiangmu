package com.erp.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解：标注在 Controller 方法上，拦截器校验当前用户是否拥有指定权限码
 * 例：@RequirePermission("base:region:add")
 * admin 角色拥有全部权限
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String value();
}
