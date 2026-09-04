package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.Result;
import com.j180.erp.dto.AssignRolesForm;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.ResetPasswordForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.dto.UserForm;
import com.j180.erp.dto.UserQuery;
import com.j180.erp.dto.UserVO;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户账号接口（角色权限-用户账号）
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @GetMapping("/page")
    @RequiresPermission("P_USER")
    public Result<PageResult<UserVO>> page(UserQuery query) {
        return Result.ok(sysUserService.page(query));
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_USER")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.ok(sysUserService.getVO(id));
    }

    @PostMapping
    @RequiresPermission("B_USER_ADD")
    @Audit(module = "用户账号", action = "新增账号", targetType = "sys_user")
    public Result<Void> create(@RequestBody UserForm form) {
        sysUserService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_USER_EDIT")
    @Audit(module = "用户账号", action = "编辑账号", targetType = "sys_user")
    public Result<Void> update(@RequestBody UserForm form) {
        sysUserService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("B_USER_STATUS")
    @Audit(module = "用户账号", action = "停用启用账号", targetType = "sys_user")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusForm form) {
        sysUserService.updateStatus(id, form);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @RequiresPermission("B_USER_RESET")
    @Audit(module = "用户账号", action = "重置密码", targetType = "sys_user")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordForm form) {
        sysUserService.resetPassword(id, form);
        return Result.ok();
    }

    @PutMapping("/{id}/roles")
    @RequiresPermission("B_USER_ROLE")
    @Audit(module = "用户账号", action = "分配角色", targetType = "sys_user")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesForm form) {
        form.setUserId(id);
        sysUserService.assignRoles(form);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_USER_DELETE")
    @Audit(module = "用户账号", action = "删除账号", targetType = "sys_user")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_USER_DELETE")
    @Audit(module = "用户账号", action = "批量删除账号", targetType = "sys_user")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        sysUserService.deleteBatch(form);
        return Result.ok();
    }
}
