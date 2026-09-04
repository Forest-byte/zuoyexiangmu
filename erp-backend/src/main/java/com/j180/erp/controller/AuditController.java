package com.j180.erp.controller;

import com.j180.erp.common.PageResult;
import com.j180.erp.common.Result;
import com.j180.erp.dto.AuditLogQuery;
import com.j180.erp.entity.AuditLog;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志接口
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @GetMapping("/page")
    @RequiresPermission("P_AUDIT")
    public Result<PageResult<AuditLog>> page(AuditLogQuery query) {
        return Result.ok(auditLogService.page(query));
    }
}
