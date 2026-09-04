package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.*;
import com.erp.service.SystemConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共配置接口：车辆/会议室/系统参数/字典/编码规则/审批规则/审计
 */
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private final SystemConfigService configService;

    public SystemConfigController(SystemConfigService configService) {
        this.configService = configService;
    }

    // ===== 车辆 =====
    @GetMapping("/vehicles")
    @RequirePermission("base:vehicle:list")
    public Result<List<SysVehicle>> vehicles() { return Result.ok(configService.vehicles()); }

    @PostMapping("/vehicles")
    @RequirePermission("base:vehicle:add")
    public Result<Void> saveVehicle(@RequestBody SysVehicle v) { configService.saveVehicle(v); return Result.ok(null); }

    @DeleteMapping("/vehicles/{id}")
    @RequirePermission("base:vehicle:del")
    public Result<Void> deleteVehicle(@PathVariable Long id) { configService.deleteVehicle(id); return Result.ok(null); }

    // ===== 会议室 =====
    @GetMapping("/meetings")
    @RequirePermission("base:meeting:list")
    public Result<List<SysMeeting>> meetings() { return Result.ok(configService.meetings()); }

    @PostMapping("/meetings")
    @RequirePermission("base:meeting:add")
    public Result<Void> saveMeeting(@RequestBody SysMeeting m) { configService.saveMeeting(m); return Result.ok(null); }

    @DeleteMapping("/meetings/{id}")
    @RequirePermission("base:meeting:del")
    public Result<Void> deleteMeeting(@PathVariable Long id) { configService.deleteMeeting(id); return Result.ok(null); }

    // ===== 系统参数 =====
    @GetMapping("/params")
    @RequirePermission("base:param:list")
    public Result<List<SysParam>> params() { return Result.ok(configService.params()); }

    @PostMapping("/params")
    @RequirePermission("base:param:add")
    public Result<Void> saveParam(@RequestBody SysParam p) { configService.saveParam(p); return Result.ok(null); }

    @DeleteMapping("/params/{id}")
    @RequirePermission("base:param:del")
    public Result<Void> deleteParam(@PathVariable Long id) { configService.deleteParam(id); return Result.ok(null); }

    // ===== 数据字典 =====
    @GetMapping("/dicts")
    @RequirePermission("base:dict:list")
    public Result<List<SysDict>> dicts(@RequestParam(required = false) String dictType) { return Result.ok(configService.dicts(dictType)); }

    @GetMapping("/dicts/types")
    @RequirePermission("base:dict:list")
    public Result<List<String>> dictTypes() { return Result.ok(configService.dictTypes()); }

    @PostMapping("/dicts")
    @RequirePermission("base:dict:add")
    public Result<Void> saveDict(@RequestBody SysDict d) { configService.saveDict(d); return Result.ok(null); }

    @DeleteMapping("/dicts/{id}")
    @RequirePermission("base:dict:del")
    public Result<Void> deleteDict(@PathVariable Long id) { configService.deleteDict(id); return Result.ok(null); }

    // ===== 编码规则 =====
    @GetMapping("/code-rules")
    @RequirePermission("base:coderule:list")
    public Result<List<SysCodeRule>> codeRules() { return Result.ok(configService.codeRules()); }

    @PostMapping("/code-rules")
    @RequirePermission("base:coderule:add")
    public Result<Void> saveCodeRule(@RequestBody SysCodeRule r) { configService.saveCodeRule(r); return Result.ok(null); }

    @DeleteMapping("/code-rules/{id}")
    @RequirePermission("base:coderule:del")
    public Result<Void> deleteCodeRule(@PathVariable Long id) { configService.deleteCodeRule(id); return Result.ok(null); }

    // ===== 审批规则 =====
    @GetMapping("/approval-rules")
    @RequirePermission("base:approvalrule:list")
    public Result<List<ApprovalRule>> approvalRules() { return Result.ok(configService.approvalRules()); }

    @PostMapping("/approval-rules")
    @RequirePermission("base:approvalrule:add")
    public Result<Void> saveApprovalRule(@RequestBody ApprovalRule r) { configService.saveApprovalRule(r); return Result.ok(null); }

    @DeleteMapping("/approval-rules/{id}")
    @RequirePermission("base:approvalrule:del")
    public Result<Void> deleteApprovalRule(@PathVariable Long id) { configService.deleteApprovalRule(id); return Result.ok(null); }

    // ===== 审计日志 =====
    @GetMapping("/audit-logs")
    @RequirePermission("system:audit:list")
    public Result<PageResult<SysAuditLog>> auditLogs(@RequestParam(required = false) String operator,
                                                     @RequestParam(required = false) String action,
                                                     @RequestParam(required = false) String startTime,
                                                     @RequestParam(required = false) String endTime,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(configService.auditPage(operator, action, startTime, endTime, page, pageSize));
    }
}
