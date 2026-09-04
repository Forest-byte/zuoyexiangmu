package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.Result;
import com.j180.erp.dto.DepartmentForm;
import com.j180.erp.dto.DeptTreeNode;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Department;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门信息接口（树形）
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/tree")
    @RequiresPermission("P_DEPARTMENT")
    public Result<List<DeptTreeNode>> tree(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status) {
        return Result.ok(departmentService.tree(keyword, status));
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_DEPARTMENT")
    public Result<Department> detail(@PathVariable Long id) {
        return Result.ok(departmentService.getById(id));
    }

    @PostMapping
    @RequiresPermission("B_DEPT_ADD")
    @Audit(module = "部门管理", action = "新增部门", targetType = "sys_department")
    public Result<Void> create(@RequestBody DepartmentForm form) {
        departmentService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_DEPT_EDIT")
    @Audit(module = "部门管理", action = "编辑部门", targetType = "sys_department")
    public Result<Void> update(@RequestBody DepartmentForm form) {
        departmentService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("B_DEPT_STATUS")
    @Audit(module = "部门管理", action = "停用启用部门", targetType = "sys_department")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusForm form) {
        departmentService.updateStatus(id, form);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_DEPT_DELETE")
    @Audit(module = "部门管理", action = "删除部门", targetType = "sys_department")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_DEPT_DELETE")
    @Audit(module = "部门管理", action = "批量删除部门", targetType = "sys_department")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        departmentService.deleteBatch(form);
        return Result.ok();
    }
}
