package com.j180.erp.common.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * IP 工具类
 */
public final class IpUtil {

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                int index = ip.indexOf(',');
                return index > 0 ? ip.substring(0, index).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
