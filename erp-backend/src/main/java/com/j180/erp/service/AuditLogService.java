package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.common.util.IpUtil;
import com.j180.erp.dto.AuditLogQuery;
import com.j180.erp.entity.AuditLog;
import com.j180.erp.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 审计日志服务：只增不改不删，支持条件分页查询与导出
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    /** 快照字段最大存储长度（超出截断） */
    private static final int SNAPSHOT_MAX = 2000;

    private final AuditLogMapper auditLogMapper;

    /**
     * 记录审计日志（带请求来源 IP）
     */
    public void record(Long userId, String username, String module, String actionType, String targetType,
                       Long targetId, String beforeSnapshot, String afterSnapshot, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeSnapshot(clip(beforeSnapshot));
        log.setAfterSnapshot(clip(afterSnapshot));
        log.setIp(request == null ? null : IpUtil.getClientIp(request));
        auditLogMapper.insert(log);
    }

    /**
     * 记录审计日志（系统级/登录失败等无用户场景）
     */
    public void record(String module, String actionType, String targetType, String message, HttpServletRequest request) {
        record(null, null, module, actionType, targetType, null, null, message, request);
    }

    /**
     * 条件分页查询审计日志
     */
    public PageResult<AuditLog> page(AuditLogQuery query) {
        AssertUtil.notNull(query, "查询条件不能为空");
        query.validatePaging();
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(AuditLog::getModule, query.getKeyword())
                    .or().like(AuditLog::getActionType, query.getKeyword())
                    .or().like(AuditLog::getUsername, query.getKeyword())
                    .or().like(AuditLog::getTargetType, query.getKeyword()));
        }
        wrapper.like(StringUtils.hasText(query.getUsername()), AuditLog::getUsername, query.getUsername());
        wrapper.eq(StringUtils.hasText(query.getModule()), AuditLog::getModule, query.getModule());
        wrapper.eq(StringUtils.hasText(query.getActionType()), AuditLog::getActionType, query.getActionType());
        wrapper.ge(query.getStartTime() != null, AuditLog::getCreateTime, query.getStartTime());
        wrapper.le(query.getEndTime() != null, AuditLog::getCreateTime, query.getEndTime());
        wrapper.orderByDesc(AuditLog::getId);
        Page<AuditLog> page = auditLogMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page);
    }

    private String clip(String snapshot) {
        if (snapshot == null) {
            return null;
        }
        return snapshot.length() > SNAPSHOT_MAX ? snapshot.substring(0, SNAPSHOT_MAX) : snapshot;
    }
}
