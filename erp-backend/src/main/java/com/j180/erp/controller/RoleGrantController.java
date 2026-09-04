package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.Result;
import com.j180.erp.dto.AssignUsersForm;
import com.j180.erp.dto.DataScopeForm;
import com.j180.erp.dto.RoleGrantForm;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.RoleGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色授权接口（功能权限 / 数据权限 / 用户分配）
 */
@RestController
@RequestMapping("/api/role-grant")
@RequiredArgsConstructor
public class RoleGrantController {

    private final RoleGrantService roleGrantService;

    @PutMapping("/resources")
    @RequiresPermission("B_GRANT_FUNC")
    @Audit(module = "角色授权", action = "功能权限授权", targetType = "sys_role")
    public Result<Void> grantResources(@RequestBody RoleGrantForm form) {
        roleGrantService.grant(form);
        return Result.ok();
    }

    @PutMapping("/data-scope")
    @RequiresPermission("B_GRANT_DATA")
    @Audit(module = "角色授权", action = "数据权限设置", targetType = "sys_role")
    public Result<Void> dataScope(@RequestBody DataScopeForm form) {
        roleGrantService.dataScope(form);
        return Result.ok();
    }

    @PutMapping("/users")
    @RequiresPermission("B_GRANT_USER")
    @Audit(module = "角色授权", action = "用户分配", targetType = "sys_role")
    public Result<Void> assignUsers(@RequestBody AssignUsersForm form) {
        roleGrantService.assignUsers(form);
        return Result.ok();
    }
}
