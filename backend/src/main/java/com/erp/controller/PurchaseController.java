package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.*;
import com.erp.service.PurchaseService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 采购业务接口：需求/采购单/审批/调度/到货/票据/跟单/结算
 */
@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    // ===== 采购需求 =====
    @GetMapping("/demands")
    @RequirePermission("inventory:purchase:list")
    public Result<PageResult<PurchaseDemand>> demands(@RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(purchaseService.demandPage(status, page, pageSize));
    }

    @PostMapping("/demands")
    @RequirePermission("inventory:purchase:add")
    public Result<PurchaseDemand> saveDemand(@RequestBody PurchaseDemand d) { return Result.ok(purchaseService.saveDemand(d)); }

    @DeleteMapping("/demands/{id}")
    @RequirePermission("inventory:purchase:del")
    public Result<Void> deleteDemand(@PathVariable Long id) { purchaseService.deleteDemand(id); return Result.ok(null); }

    @PostMapping("/demands/to-order")
    @RequirePermission("inventory:purchase:add")
    public Result<PurchaseOrder> toOrder(@RequestBody Map<String, Object> body) {
        List<Long> demandIds = ((List<Number>) body.get("demandIds")).stream().map(Number::longValue).toList();
        Long supplierId = Long.valueOf(String.valueOf(body.get("supplierId")));
        Long warehouseId = body.get("warehouseId") == null ? null : Long.valueOf(String.valueOf(body.get("warehouseId")));
        LocalDate applyDate = body.get("applyDate") == null ? null : LocalDate.parse(String.valueOf(body.get("applyDate")));
        return Result.ok(purchaseService.createFromDemands(demandIds, supplierId, warehouseId, applyDate));
    }

    // ===== 采购单 =====
    @GetMapping("/orders")
    @RequirePermission("inventory:purchase:list")
    public Result<PageResult<PurchaseOrder>> orders(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(purchaseService.orderPage(keyword, status, page, pageSize));
    }

    @GetMapping("/orders/{id}")
    @RequirePermission("inventory:purchase:list")
    public Result<PurchaseOrder> orderDetail(@PathVariable Long id) { return Result.ok(purchaseService.orderDetail(id)); }

    @PostMapping("/orders/save")
    @RequirePermission("inventory:purchase:add")
    public Result<PurchaseOrder> saveOrder(@RequestBody PurchaseOrder po) { return Result.ok(purchaseService.saveOrder(po)); }

    @DeleteMapping("/orders/{id}")
    @RequirePermission("inventory:purchase:del")
    public Result<Void> deleteOrder(@PathVariable Long id) { purchaseService.deleteOrder(id); return Result.ok(null); }

    @PostMapping("/orders/{id}/submit")
    @RequirePermission("inventory:purchase:approve")
    public Result<Void> submit(@PathVariable Long id) { purchaseService.submitApprove(id); return Result.ok(null); }

    @PostMapping("/orders/{id}/approve")
    @RequirePermission("inventory:purchase:approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        purchaseService.approve(id, pass, (String) body.get("comment"));
        return Result.ok(null);
    }

    @PostMapping("/orders/{id}/dispatch")
    @RequirePermission("inventory:purchase:edit")
    public Result<Void> dispatch(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        purchaseService.dispatchVehicle(id, Long.valueOf(String.valueOf(body.get("vehicleId"))));
        return Result.ok(null);
    }

    @PostMapping("/orders/{id}/arrival")
    @RequirePermission("inventory:purchase:edit")
    public Result<WmsInbound> arrival(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long warehouseId = body.get("warehouseId") == null ? null : Long.valueOf(String.valueOf(body.get("warehouseId")));
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return Result.ok(purchaseService.arrival(id, warehouseId, items));
    }

    @PostMapping("/orders/{id}/settle")
    @RequirePermission("inventory:purchase:approve")
    public Result<Void> settle(@PathVariable Long id) { purchaseService.settle(id); return Result.ok(null); }

    // ===== 采购票据 =====
    @GetMapping("/bills")
    @RequirePermission("inventory:purchase:list")
    public Result<List<PurchaseBill>> bills() { return Result.ok(purchaseService.bills()); }

    @PostMapping("/bills")
    @RequirePermission("inventory:purchase:add")
    public Result<Void> registerBill(@RequestBody PurchaseBill b) { purchaseService.registerBill(b); return Result.ok(null); }

    // ===== 采购跟单 =====
    @GetMapping("/follow-ups")
    @RequirePermission("inventory:purchase:list")
    public Result<List<FollowUps>> followUps(@RequestParam Long orderId) { return Result.ok(purchaseService.followUps(orderId)); }

    @PostMapping("/follow-ups/complete")
    @RequirePermission("inventory:purchase:edit")
    public Result<Void> completeNode(@RequestBody Map<String, Object> body) {
        purchaseService.completeNode(Long.valueOf(String.valueOf(body.get("orderId"))), (String) body.get("nodeName"));
        return Result.ok(null);
    }
}
