package com.erp.service.impl;

import com.erp.entity.SysAuditLog;
import com.erp.mapper.AuditLogMapper;
import com.erp.service.AuditService;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志实现
 */
@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogMapper auditLogMapper;

    public AuditServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void log(String action, String target, String before, String after) {
        SysAuditLog log = new SysAuditLog();
        log.setOperator(UserContext.currentName());
        log.setAction(action);
        log.setTarget(target);
        log.setBeforeData(before);
        log.setAfterData(after);
        log.setTime(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
