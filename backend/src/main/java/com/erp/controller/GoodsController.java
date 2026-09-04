package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.Goods;
import com.erp.entity.GoodsCategory;
import com.erp.entity.GoodsUnit;
import com.erp.service.GoodsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品档案接口：商品/分类/单位
 */
@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping("/list")
    @RequirePermission("inventory:goods:list")
    public Result<PageResult<Goods>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(goodsService.goodsPage(keyword, categoryId, status, page, pageSize));
    }

    @GetMapping("/all")
    @RequirePermission("inventory:goods:list")
    public Result<List<Goods>> all() { return Result.ok(goodsService.goodsAll()); }

    @GetMapping("/on-sale")
    @RequirePermission("inventory:goods:list")
    public Result<List<Goods>> onSale() { return Result.ok(goodsService.goodsOnSale()); }

    @GetMapping("/{id}")
    @RequirePermission("inventory:goods:list")
    public Result<Goods> detail(@PathVariable Long id) { return Result.ok(goodsService.goodsDetail(id)); }

    @PostMapping("/save")
    @RequirePermission("inventory:goods:add")
    public Result<Goods> save(@RequestBody Goods g) { return Result.ok(goodsService.saveGoods(g)); }

    @DeleteMapping("/{id}")
    @RequirePermission("inventory:goods:del")
    public Result<Void> delete(@PathVariable Long id) { goodsService.deleteGoods(id); return Result.ok(null); }

    @PostMapping("/{id}/limits")
    @RequirePermission("inventory:goods:edit")
    public Result<Void> updateLimits(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal low = new BigDecimal(String.valueOf(body.getOrDefault("lowLimit", "0")));
        BigDecimal high = body.get("highLimit") == null ? null : new BigDecimal(String.valueOf(body.get("highLimit")));
        goodsService.updateLimits(id, low, high);
        return Result.ok(null);
    }

    // ===== 分类 =====
    @GetMapping("/categories/tree")
    @RequirePermission("inventory:goods:list")
    public Result<List<GoodsCategory>> categories() { return Result.ok(goodsService.categoryTree()); }

    @PostMapping("/categories")
    @RequirePermission("inventory:goods:add")
    public Result<Void> saveCategory(@RequestBody GoodsCategory c) { goodsService.saveCategory(c); return Result.ok(null); }

    @DeleteMapping("/categories/{id}")
    @RequirePermission("inventory:goods:del")
    public Result<Void> deleteCategory(@PathVariable Long id) { goodsService.deleteCategory(id); return Result.ok(null); }

    // ===== 单位 =====
    @GetMapping("/units")
    @RequirePermission("inventory:goods:list")
    public Result<List<GoodsUnit>> units() { return Result.ok(goodsService.units()); }

    @PostMapping("/units")
    @RequirePermission("inventory:goods:add")
    public Result<Void> saveUnit(@RequestBody GoodsUnit u) { goodsService.saveUnit(u); return Result.ok(null); }

    @DeleteMapping("/units/{id}")
    @RequirePermission("inventory:goods:del")
    public Result<Void> deleteUnit(@PathVariable Long id) { goodsService.deleteUnit(id); return Result.ok(null); }
}
