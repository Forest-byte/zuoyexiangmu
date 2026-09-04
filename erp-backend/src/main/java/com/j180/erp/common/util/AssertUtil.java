package com.j180.erp.common.util;

import com.j180.erp.common.BizException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 断言工具类：校验失败抛出业务异常
 */
public final class AssertUtil {

    private AssertUtil() {
    }

    /** 表达式必须为 true */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BizException(message);
        }
    }

    /** 表达式必须为 false */
    public static void isFalse(boolean expression, String message) {
        isTrue(!expression, message);
    }

    /** 对象不能为 null */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(message);
        }
    }

    /** 对象必须为 null（一般用于唯一性冲突检查） */
    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new BizException(message);
        }
    }

    /** 字符串不能为空白 */
    public static void notBlank(String str, String message) {
        if (!StringUtils.hasText(str)) {
            throw new BizException(message);
        }
    }

    /** 集合不能为空 */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(message);
        }
    }

    /** Map 不能为空 */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new BizException(message);
        }
    }

    /** 数组/可变参数不能为空 */
    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new BizException(message);
        }
    }

    /** 字符串长度不能超过 max */
    public static void maxLength(String str, int max, String message) {
        if (str != null && str.length() > max) {
            throw new BizException(message);
        }
    }

    /** 字符串必须符合正则 */
    public static void matches(String str, String regex, String message) {
        if (StringUtils.hasText(str) && !str.matches(regex)) {
            throw new BizException(message);
        }
    }

    /** 必填字符串：非空白且长度不超限 */
    public static void text(String str, int max, String message) {
        notBlank(str, message);
        maxLength(str, max, message + "，长度不能超过" + max + "个字符");
    }
}
