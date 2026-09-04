package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.*;
import com.erp.service.CrmService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * CRM 接口：客户/供应商/分类/跟进/信用/应收应付/合并
 */
@RestController
@RequestMapping("/api/crm")
public class CrmController {

    private final CrmService crmService;

    public CrmController(CrmService crmService) {
        this.crmService = crmService;
    }

    // ===== 客户 =====
    @GetMapping("/customers")
    @RequirePermission("crm:customer:list")
    public Result<PageResult<CrmCustomer>> customers(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(crmService.customerPage(keyword, status, page, pageSize));
    }

    @GetMapping("/customers/all")
    @RequirePermission("crm:customer:list")
    public Result<List<CrmCustomer>> customerAll() { return Result.ok(crmService.customers()); }

    @GetMapping("/customers/{id}")
    @RequirePermission("crm:customer:list")
    public Result<CrmCustomer> customerDetail(@PathVariable Long id) { return Result.ok(crmService.customerDetail(id)); }

    @PostMapping("/customers")
    @RequirePermission("crm:customer:add")
    public Result<CrmCustomer> saveCustomer(@RequestBody CrmCustomer c) { return Result.ok(crmService.saveCustomer(c)); }

    @PostMapping("/customers/{id}/submit")
    @RequirePermission("crm:customer:add")
    public Result<Void> submitCustomer(@PathVariable Long id) { crmService.submitCustomer(id); return Result.ok(null); }

    @PostMapping("/customers/{id}/approve")
    @RequirePermission("crm:customer:approve")
    public Result<Void> approveCustomer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        crmService.approveCustomer(id, pass, (String) body.get("comment"));
        return Result.ok(null);
    }

    @DeleteMapping("/customers/{id}")
    @RequirePermission("crm:customer:del")
    public Result<Void> deleteCustomer(@PathVariable Long id) { crmService.deleteCustomer(id); return Result.ok(null); }

    // ===== 供应商 =====
    @GetMapping("/suppliers")
    @RequirePermission("crm:supplier:list")
    public Result<PageResult<CrmSupplier>> suppliers(@RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(crmService.supplierPage(keyword, page, pageSize));
    }

    @GetMapping("/suppliers/all")
    @RequirePermission("crm:supplier:list")
    public Result<List<CrmSupplier>> supplierAll() { return Result.ok(crmService.suppliers()); }

    @PostMapping("/suppliers")
    @RequirePermission("crm:supplier:add")
    public Result<CrmSupplier> saveSupplier(@RequestBody CrmSupplier s) { return Result.ok(crmService.saveSupplier(s)); }

    @DeleteMapping("/suppliers/{id}")
    @RequirePermission("crm:supplier:del")
    public Result<Void> deleteSupplier(@PathVariable Long id) { crmService.deleteSupplier(id); return Result.ok(null); }

    // ===== 分类 =====
    @GetMapping("/categories/tree")
    @RequirePermission("crm:customer:list")
    public Result<List<CrmCategory>> categoryTree(@RequestParam(required = false) String kind) { return Result.ok(crmService.categoryTree(kind)); }

    @PostMapping("/categories")
    @RequirePermission("crm:customer:add")
    public Result<Void> saveCategory(@RequestBody CrmCategory c) { crmService.saveCategory(c); return Result.ok(null); }

    @DeleteMapping("/categories/{id}")
    @RequirePermission("crm:customer:del")
    public Result<Void> deleteCategory(@PathVariable Long id) { crmService.deleteCategory(id); return Result.ok(null); }

    // ===== 跟进 =====
    @GetMapping("/follows")
    @RequirePermission("crm:follow:list")
    public Result<List<CrmFollowRecord>> follows(@RequestParam Long customerId) { return Result.ok(crmService.follows(customerId)); }

    @PostMapping("/follows")
    @RequirePermission("crm:follow:add")
    public Result<Void> addFollow(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(String.valueOf(body.get("customerId")));
        String content = (String) body.get("content");
        LocalDate nextTime = body.get("nextTime") == null ? null : LocalDate.parse(String.valueOf(body.get("nextTime")));
        crmService.addFollow(customerId, content, nextTime);
        return Result.ok(null);
    }

    // ===== 信用 =====
    @GetMapping("/credit")
    @RequirePermission("crm:credit:list")
    public Result<Map<String, Object>> creditInfo(@RequestParam Long customerId) { return Result.ok(crmService.creditInfo(customerId)); }

    @PostMapping("/credit/change")
    @RequirePermission("crm:credit:edit")
    public Result<Void> changeCredit(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(String.valueOf(body.get("customerId")));
        BigDecimal newLimit = new BigDecimal(String.valueOf(body.get("newLimit")));
        crmService.changeCredit(customerId, newLimit, (String) body.get("reason"));
        return Result.ok(null);
    }

    // ===== 应收应付 =====
    @GetMapping("/arc")
    @RequirePermission("crm:reconcile:list")
    public Result<PageResult<CrmArcDetail>> arc(@RequestParam(required = false) Long customerId,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(crmService.arcPage(customerId, status, page, pageSize));
    }

    @GetMapping("/ap")
    @RequirePermission("crm:reconcile:list")
    public Result<PageResult<CrmApDetail>> ap(@RequestParam(required = false) Long supplierId,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(crmService.apPage(supplierId, status, page, pageSize));
    }

    // ===== 客户合并 =====
    @PostMapping("/customers/merge")
    @RequirePermission("crm:customer:edit")
    public Result<Void> merge(@RequestBody Map<String, Object> body) {
        Long fromId = Long.valueOf(String.valueOf(body.get("fromId")));
        Long toId = Long.valueOf(String.valueOf(body.get("toId")));
        crmService.mergeCustomer(fromId, toId);
        return Result.ok(null);
    }
}
