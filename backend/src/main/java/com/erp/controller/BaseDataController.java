package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.*;
import com.erp.service.BaseDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础维护接口：地区/分公司/部门/员工/仓库
 */
@RestController
@RequestMapping("/api/base")
public class BaseDataController {

    private final BaseDataService baseDataService;

    public BaseDataController(BaseDataService baseDataService) {
        this.baseDataService = baseDataService;
    }

    // ===== 地区 =====
    @GetMapping("/regions/tree")
    @RequirePermission("base:region:list")
    public Result<List<SysRegion>> regionTree() { return Result.ok(baseDataService.regionTree()); }

    @PostMapping("/regions")
    @RequirePermission("base:region:add")
    public Result<Void> saveRegion(@RequestBody SysRegion r) { baseDataService.saveRegion(r); return Result.ok(null); }

    @DeleteMapping("/regions/{id}")
    @RequirePermission("base:region:del")
    public Result<Void> deleteRegion(@PathVariable Long id) { baseDataService.deleteRegion(id); return Result.ok(null); }

    // ===== 分公司 =====
    @GetMapping("/companies")
    @RequirePermission("base:company:list")
    public Result<List<SysCompany>> companies() { return Result.ok(baseDataService.companies()); }

    @PostMapping("/companies")
    @RequirePermission("base:company:add")
    public Result<Void> saveCompany(@RequestBody SysCompany c) { baseDataService.saveCompany(c); return Result.ok(null); }

    @DeleteMapping("/companies/{id}")
    @RequirePermission("base:company:del")
    public Result<Void> deleteCompany(@PathVariable Long id) { baseDataService.deleteCompany(id); return Result.ok(null); }

    // ===== 部门 =====
    @GetMapping("/depts")
    @RequirePermission("base:dept:list")
    public Result<List<SysDept>> depts(@RequestParam(required = false) Long companyId) { return Result.ok(baseDataService.depts(companyId)); }

    @PostMapping("/depts")
    @RequirePermission("base:dept:add")
    public Result<Void> saveDept(@RequestBody SysDept d) { baseDataService.saveDept(d); return Result.ok(null); }

    @DeleteMapping("/depts/{id}")
    @RequirePermission("base:dept:del")
    public Result<Void> deleteDept(@PathVariable Long id) { baseDataService.deleteDept(id); return Result.ok(null); }

    // ===== 员工 =====
    @GetMapping("/employees")
    @RequirePermission("base:employee:list")
    public Result<PageResult<SysEmployee>> employees(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Long deptId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(baseDataService.employeePage(keyword, deptId, page, pageSize));
    }

    @PostMapping("/employees")
    @RequirePermission("base:employee:add")
    public Result<Void> saveEmployee(@RequestBody SysEmployee e) { baseDataService.saveEmployee(e); return Result.ok(null); }

    @DeleteMapping("/employees/{id}")
    @RequirePermission("base:employee:del")
    public Result<Void> deleteEmployee(@PathVariable Long id) { baseDataService.deleteEmployee(id); return Result.ok(null); }

    // ===== 仓库 =====
    @GetMapping("/warehouses")
    @RequirePermission("base:warehouse:list")
    public Result<List<SysWarehouse>> warehouses(@RequestParam(required = false) String keyword) { return Result.ok(baseDataService.warehouses(keyword)); }

    @PostMapping("/warehouses")
    @RequirePermission("base:warehouse:add")
    public Result<Void> saveWarehouse(@RequestBody SysWarehouse w) { baseDataService.saveWarehouse(w); return Result.ok(null); }

    @DeleteMapping("/warehouses/{id}")
    @RequirePermission("base:warehouse:del")
    public Result<Void> deleteWarehouse(@PathVariable Long id) { baseDataService.deleteWarehouse(id); return Result.ok(null); }
}
