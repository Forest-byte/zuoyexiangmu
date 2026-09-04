package com.erp.service;

/**
 * 审计日志服务
 */
public interface AuditService {
    void log(String action, String target, String before, String after);
}
