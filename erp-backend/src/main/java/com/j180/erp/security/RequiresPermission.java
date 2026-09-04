package com.j180.erp.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能权限注解：标注在 Controller 方法上，声明访问该接口所需的资源编码；
 * 由鉴权拦截器统一校验（服务端强制校验，前端隐藏不作为安全边界）
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /** 资源编码，如 P_COMPANY / B_COMPANY_ADD */
    String value();
}
