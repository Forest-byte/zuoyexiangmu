package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.Result;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.dto.WarehouseForm;
import com.j180.erp.dto.WarehouseQuery;
import com.j180.erp.entity.Warehouse;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仓库信息接口
 */
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/page")
    @RequiresPermission("P_WAREHOUSE")
    public Result<PageResult<Warehouse>> page(WarehouseQuery query) {
        return Result.ok(warehouseService.page(query));
    }

    @GetMapping("/list")
    @RequiresPermission("P_WAREHOUSE")
    public Result<List<Warehouse>> list() {
        return Result.ok(warehouseService.listEnabled());
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_WAREHOUSE")
    public Result<Warehouse> detail(@PathVariable Long id) {
        return Result.ok(warehouseService.getById(id));
    }

    @PostMapping
    @RequiresPermission("B_WH_ADD")
    @Audit(module = "仓库管理", action = "新增仓库", targetType = "wms_warehouse")
    public Result<Void> create(@RequestBody WarehouseForm form) {
        warehouseService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_WH_EDIT")
    @Audit(module = "仓库管理", action = "编辑仓库", targetType = "wms_warehouse")
    public Result<Void> update(@RequestBody WarehouseForm form) {
        warehouseService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("B_WH_STATUS")
    @Audit(module = "仓库管理", action = "停用启用仓库", targetType = "wms_warehouse")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusForm form) {
        warehouseService.updateStatus(id, form);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_WH_DELETE")
    @Audit(module = "仓库管理", action = "删除仓库", targetType = "wms_warehouse")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_WH_DELETE")
    @Audit(module = "仓库管理", action = "批量删除仓库", targetType = "wms_warehouse")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        warehouseService.deleteBatch(form);
        return Result.ok();
    }
}
