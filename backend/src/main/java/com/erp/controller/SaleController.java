package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.ReturnOrder;
import com.erp.entity.SaleOrder;
import com.erp.entity.WmsOutbound;
import com.erp.service.SaleService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 销售业务接口：销售单/审批/出库发货/退货
 */
@RestController
@RequestMapping("/api/sale")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    // ===== 销售单 =====
    @GetMapping("/orders")
    @RequirePermission("inventory:sale:list")
    public Result<PageResult<SaleOrder>> orders(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(saleService.orderPage(keyword, status, page, pageSize));
    }

    @GetMapping("/orders/{id}")
    @RequirePermission("inventory:sale:list")
    public Result<SaleOrder> orderDetail(@PathVariable Long id) { return Result.ok(saleService.orderDetail(id)); }

    @PostMapping("/orders/save")
    @RequirePermission("inventory:sale:add")
    public Result<SaleOrder> saveOrder(@RequestBody SaleOrder so) { return Result.ok(saleService.saveOrder(so)); }

    @DeleteMapping("/orders/{id}")
    @RequirePermission("inventory:sale:del")
    public Result<Void> deleteOrder(@PathVariable Long id) { saleService.deleteOrder(id); return Result.ok(null); }

    @PostMapping("/orders/{id}/submit")
    @RequirePermission("inventory:sale:approve")
    public Result<Void> submit(@PathVariable Long id) { saleService.submitApprove(id); return Result.ok(null); }

    @PostMapping("/orders/{id}/approve")
    @RequirePermission("inventory:sale:approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        saleService.approve(id, pass, (String) body.get("comment"));
        return Result.ok(null);
    }

    @PostMapping("/orders/{id}/deliver")
    @RequirePermission("inventory:sale:edit")
    public Result<WmsOutbound> deliver(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long warehouseId = Long.valueOf(String.valueOf(body.get("warehouseId")));
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return Result.ok(saleService.deliver(id, warehouseId, items));
    }

    // ===== 销售退货 =====
    @GetMapping("/returns")
    @RequirePermission("inventory:return:list")
    public Result<PageResult<ReturnOrder>> returns(@RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(saleService.returnPage(status, page, pageSize));
    }

    @PostMapping("/returns")
    @RequirePermission("inventory:return:add")
    public Result<ReturnOrder> saveReturn(@RequestBody Map<String, Object> body) {
        ReturnOrder ro = new ReturnOrder();
        ro.setSrcId(Long.valueOf(String.valueOf(body.get("srcId"))));
        ro.setReason((String) body.get("reason"));
        ro.setAmount(new BigDecimal(String.valueOf(body.get("amount"))));
        Long goodsId = Long.valueOf(String.valueOf(body.get("goodsId")));
        Long warehouseId = Long.valueOf(String.valueOf(body.get("warehouseId")));
        BigDecimal quantity = new BigDecimal(String.valueOf(body.get("quantity")));
        return Result.ok(saleService.saveReturn(ro, goodsId, warehouseId, quantity));
    }
}
