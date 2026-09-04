package com.j180.erp.common.util;

import org.springframework.util.StringUtils;

/**
 * 敏感信息脱敏工具
 */
public final class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 身份证号脱敏：保留前6后4
     */
    public static String idCard(String idCard) {
        if (!StringUtils.hasText(idCard) || idCard.length() < 11) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏：保留前3后4
     */
    public static String mobile(String mobile) {
        if (!StringUtils.hasText(mobile) || mobile.length() < 8) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    /**
     * 银行账号脱敏：保留后4
     */
    public static String bankAccount(String account) {
        if (!StringUtils.hasText(account) || account.length() < 8) {
            return account;
        }
        return "************" + account.substring(account.length() - 4);
    }
}
