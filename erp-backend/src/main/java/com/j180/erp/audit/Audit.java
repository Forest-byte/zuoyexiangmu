package com.j180.erp.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解：标注在 Controller 方法上，自动记录操作审计（含变更后快照，密码脱敏）
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /** 所属模块，如 "公司管理" */
    String module();

    /** 操作类型，如 "新增公司" */
    String action();

    /** 操作对象类型，如 "sys_company" */
    String targetType() default "";
}
