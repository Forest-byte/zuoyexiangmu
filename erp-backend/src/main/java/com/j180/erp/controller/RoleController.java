package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.Result;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.RoleForm;
import com.j180.erp.dto.RoleQuery;
import com.j180.erp.dto.RoleVO;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Role;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.RoleService;
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
 * 角色维护接口
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    @RequiresPermission("P_ROLE")
    public Result<PageResult<RoleVO>> page(RoleQuery query) {
        return Result.ok(roleService.page(query));
    }

    @GetMapping("/list")
    @RequiresPermission("P_ROLE")
    public Result<List<Role>> list() {
        return Result.ok(roleService.listEnabled());
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_ROLE")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.ok(roleService.getVO(id));
    }

    @PostMapping
    @RequiresPermission("B_ROLE_ADD")
    @Audit(module = "角色维护", action = "新增角色", targetType = "sys_role")
    public Result<Void> create(@RequestBody RoleForm form) {
        roleService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_ROLE_EDIT")
    @Audit(module = "角色维护", action = "编辑角色", targetType = "sys_role")
    public Result<Void> update(@RequestBody RoleForm form) {
        roleService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("B_ROLE_STATUS")
    @Audit(module = "角色维护", action = "停用启用角色", targetType = "sys_role")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusForm form) {
        roleService.updateStatus(id, form);
        return Result.ok();
    }

    @PostMapping("/{id}/copy")
    @RequiresPermission("B_ROLE_COPY")
    @Audit(module = "角色维护", action = "复制角色", targetType = "sys_role")
    public Result<Long> copy(@PathVariable Long id, @RequestBody RoleForm form) {
        return Result.ok(roleService.copy(id, form));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_ROLE_DELETE")
    @Audit(module = "角色维护", action = "删除角色", targetType = "sys_role")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_ROLE_DELETE")
    @Audit(module = "角色维护", action = "批量删除角色", targetType = "sys_role")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        roleService.deleteBatch(form);
        return Result.ok();
    }
}
