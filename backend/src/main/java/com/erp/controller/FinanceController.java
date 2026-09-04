package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.FinAccount;
import com.erp.entity.FinAccountLog;
import com.erp.entity.FinConList;
import com.erp.entity.FinTransfer;
import com.erp.service.FinanceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 财务接口：账户/收款核销/付款核销/内部转账/资金流水
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    // ===== 账户 =====
    @GetMapping("/accounts")
    @RequirePermission("finance:account:list")
    public Result<List<FinAccount>> accounts() { return Result.ok(financeService.accounts()); }

    @PostMapping("/accounts")
    @RequirePermission("finance:account:add")
    public Result<FinAccount> saveAccount(@RequestBody FinAccount a) { return Result.ok(financeService.saveAccount(a)); }

    @DeleteMapping("/accounts/{id}")
    @RequirePermission("finance:account:del")
    public Result<Void> deleteAccount(@PathVariable Long id) { financeService.deleteAccount(id); return Result.ok(null); }

    // ===== 收款核销 =====
    @GetMapping("/receipts")
    @RequirePermission("finance:receipt:list")
    public Result<PageResult<FinConList>> receipts(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(financeService.receiptPage(keyword, page, pageSize));
    }

    @PostMapping("/receipts")
    @RequirePermission("finance:receipt:add")
    public Result<FinConList> receipt(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(String.valueOf(body.get("customerId")));
        Long accountId = Long.valueOf(String.valueOf(body.get("accountId")));
        String payType = (String) body.get("payType");
        LocalDate date = body.get("receiptDate") == null ? null : LocalDate.parse(String.valueOf(body.get("receiptDate")));
        List<Map<String, Object>> details = (List<Map<String, Object>>) body.get("details");
        return Result.ok(financeService.receipt(customerId, accountId, payType, date, (String) body.get("remark"), details));
    }

    // ===== 付款核销 =====
    @GetMapping("/payments")
    @RequirePermission("finance:payment:list")
    public Result<PageResult<FinConList>> payments(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(financeService.paymentPage(keyword, page, pageSize));
    }

    @PostMapping("/payments")
    @RequirePermission("finance:payment:add")
    public Result<FinConList> payment(@RequestBody Map<String, Object> body) {
        Long supplierId = Long.valueOf(String.valueOf(body.get("supplierId")));
        Long accountId = Long.valueOf(String.valueOf(body.get("accountId")));
        String payType = (String) body.get("payType");
        LocalDate date = body.get("payDate") == null ? null : LocalDate.parse(String.valueOf(body.get("payDate")));
        List<Map<String, Object>> details = (List<Map<String, Object>>) body.get("details");
        return Result.ok(financeService.payment(supplierId, accountId, payType, date, (String) body.get("remark"), details));
    }

    // ===== 内部转账 =====
    @GetMapping("/transfers")
    @RequirePermission("finance:transfer:list")
    public Result<List<FinTransfer>> transfers() { return Result.ok(financeService.transfers()); }

    @PostMapping("/transfers")
    @RequirePermission("finance:transfer:add")
    public Result<FinTransfer> createTransfer(@RequestBody FinTransfer t) { return Result.ok(financeService.createTransfer(t)); }

    @PostMapping("/transfers/{id}/approve")
    @RequirePermission("finance:transfer:approve")
    public Result<Void> approveTransfer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        financeService.approveTransfer(id, pass, (String) body.get("comment"));
        return Result.ok(null);
    }

    // ===== 资金流水 =====
    @GetMapping("/account-logs")
    @RequirePermission("finance:account:list")
    public Result<PageResult<FinAccountLog>> accountLogs(@RequestParam(required = false) Long accountId,
                                                         @RequestParam(required = false) String bizType,
                                                         @RequestParam(required = false) String refNo,
                                                         @RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(financeService.accountLogPage(accountId, bizType, refNo, startDate, endDate, page, pageSize));
    }
}
