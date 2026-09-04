package com.j180.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.j180.erp.entity.AuditLog;

/**
 * 审计日志 Mapper（仅查询，审计日志只增不改不删）
 */
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
