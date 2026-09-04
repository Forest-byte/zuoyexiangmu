package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.*;
import com.erp.service.WmsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仓储管理接口：入库/出库/库存/流水/盘点/调拨
 */
@RestController
@RequestMapping("/api/wms")
public class WmsController {

    private final WmsService wmsService;

    public WmsController(WmsService wmsService) {
        this.wmsService = wmsService;
    }

    // ===== 入库单 =====
    @GetMapping("/inbounds")
    @RequirePermission("warehouse:inbound:list")
    public Result<PageResult<WmsInbound>> inbounds(@RequestParam(required = false) String inType,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(wmsService.inboundPage(inType, keyword, page, pageSize));
    }

    @GetMapping("/inbounds/{id}")
    @RequirePermission("warehouse:inbound:list")
    public Result<WmsInbound> inboundDetail(@PathVariable Long id) { return Result.ok(wmsService.inboundDetail(id)); }

    @PostMapping("/inbounds/manual")
    @RequirePermission("warehouse:inbound:add")
    public Result<WmsInbound> manualInbound(@RequestBody Map<String, Object> body) {
        WmsInbound in = new WmsInbound();
        in.setInType((String) body.get("inType"));
        in.setSrcNo((String) body.get("srcNo"));
        in.setWarehouseId(Long.valueOf(String.valueOf(body.get("warehouseId"))));
        List<WmsInboundItem> items = convertItems(body.get("items"));
        return Result.ok(wmsService.manualInbound(in, items));
    }

    // ===== 出库单 =====
    @GetMapping("/outbounds")
    @RequirePermission("warehouse:outbound:list")
    public Result<PageResult<WmsOutbound>> outbounds(@RequestParam(required = false) String outType,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(wmsService.outboundPage(outType, keyword, page, pageSize));
    }

    @GetMapping("/outbounds/{id}")
    @RequirePermission("warehouse:outbound:list")
    public Result<WmsOutbound> outboundDetail(@PathVariable Long id) { return Result.ok(wmsService.outboundDetail(id)); }

    @PostMapping("/outbounds/manual")
    @RequirePermission("warehouse:outbound:add")
    public Result<WmsOutbound> manualOutbound(@RequestBody Map<String, Object> body) {
        WmsOutbound out = new WmsOutbound();
        out.setOutType((String) body.get("outType"));
        out.setSrcNo((String) body.get("srcNo"));
        out.setWarehouseId(Long.valueOf(String.valueOf(body.get("warehouseId"))));
        List<WmsOutboundItem> items = convertOutItems(body.get("items"));
        return Result.ok(wmsService.manualOutbound(out, items));
    }

    // ===== 库存 =====
    @GetMapping("/stocks")
    @RequirePermission("warehouse:stock:list")
    public Result<PageResult<WmsStock>> stocks(@RequestParam(required = false) Long warehouseId,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(wmsService.stockPage(warehouseId, keyword, page, pageSize));
    }

    @GetMapping("/stocks/low")
    @RequirePermission("warehouse:stock:list")
    public Result<List<WmsStock>> lowStock() { return Result.ok(wmsService.lowStock()); }

    // ===== 库存流水 =====
    @GetMapping("/stock-logs")
    @RequirePermission("warehouse:stock:list")
    public Result<PageResult<WmsStockLog>> stockLogs(@RequestParam(required = false) Long goodsId,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(wmsService.stockLogPage(goodsId, warehouseId, type, page, pageSize));
    }

    // ===== 盘点 =====
    @GetMapping("/checks")
    @RequirePermission("warehouse:check:list")
    public Result<PageResult<WmsCheck>> checks(@RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(wmsService.checkPage(status, page, pageSize));
    }

    @GetMapping("/checks/{id}")
    @RequirePermission("warehouse:check:list")
    public Result<WmsCheck> checkDetail(@PathVariable Long id) { return Result.ok(wmsService.checkDetail(id)); }

    @PostMapping("/checks")
    @RequirePermission("warehouse:check:add")
    public Result<WmsCheck> createCheck(@RequestBody Map<String, Object> body) {
        return Result.ok(wmsService.createCheck(Long.valueOf(String.valueOf(body.get("warehouseId")))));
    }

    @PostMapping("/checks/{id}/submit")
    @RequirePermission("warehouse:check:edit")
    public Result<Void> submitCheck(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<WmsCheckItem> items = convertCheckItems(body.get("items"));
        wmsService.submitCheck(id, items);
        return Result.ok(null);
    }

    // ===== 调拨 =====
    @GetMapping("/transfers")
    @RequirePermission("warehouse:transfer:list")
    public Result<PageResult<WmsTransfer>> transfers(@RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(wmsService.transferPage(status, page, pageSize));
    }

    @PostMapping("/transfers")
    @RequirePermission("warehouse:transfer:add")
    public Result<WmsTransfer> createTransfer(@RequestBody WmsTransfer t) { return Result.ok(wmsService.createTransfer(t)); }

    @PostMapping("/transfers/{id}/approve")
    @RequirePermission("warehouse:transfer:approve")
    public Result<Void> approveTransfer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        wmsService.approveTransfer(id, pass, (String) body.get("comment"));
        return Result.ok(null);
    }

    // ---- 参数转换辅助 ----
    @SuppressWarnings("unchecked")
    private List<WmsInboundItem> convertItems(Object obj) {
        if (obj == null) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        return list.stream().map(m -> {
            WmsInboundItem it = new WmsInboundItem();
            it.setGoodsId(Long.valueOf(String.valueOf(m.get("goodsId"))));
            it.setQuantity(new java.math.BigDecimal(String.valueOf(m.get("quantity"))));
            it.setPrice(m.get("price") == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(String.valueOf(m.get("price"))));
            return it;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<WmsOutboundItem> convertOutItems(Object obj) {
        if (obj == null) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        return list.stream().map(m -> {
            WmsOutboundItem it = new WmsOutboundItem();
            it.setGoodsId(Long.valueOf(String.valueOf(m.get("goodsId"))));
            it.setQuantity(new java.math.BigDecimal(String.valueOf(m.get("quantity"))));
            return it;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<WmsCheckItem> convertCheckItems(Object obj) {
        if (obj == null) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        return list.stream().map(m -> {
            WmsCheckItem it = new WmsCheckItem();
            it.setId(Long.valueOf(String.valueOf(m.get("id"))));
            it.setGoodsId(Long.valueOf(String.valueOf(m.get("goodsId"))));
            it.setBookQty(new java.math.BigDecimal(String.valueOf(m.get("bookQty"))));
            it.setRealQty(m.get("realQty") == null ? null : new java.math.BigDecimal(String.valueOf(m.get("realQty"))));
            return it;
        }).toList();
    }
}
