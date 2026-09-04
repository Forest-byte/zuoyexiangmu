package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.SysResource;
import com.erp.entity.SysRole;
import com.erp.entity.SysUser;
import com.erp.service.PermissionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限管理接口：角色/资源/用户角色授权
 */
@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // ===== 角色 =====
    @GetMapping("/roles")
    @RequirePermission("system:role:list")
    public Result<List<SysRole>> roles() { return Result.ok(permissionService.roles()); }

    @PostMapping("/roles")
    @RequirePermission("system:role:add")
    public Result<Void> saveRole(@RequestBody SysRole role) { permissionService.saveRole(role); return Result.ok(null); }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("system:role:del")
    public Result<Void> deleteRole(@PathVariable Long id) { permissionService.deleteRole(id); return Result.ok(null); }

    // ===== 资源 =====
    @GetMapping("/resources/tree")
    @RequirePermission("system:resource:list")
    public Result<List<SysResource>> resourceTree() { return Result.ok(permissionService.resourceTree()); }

    @PostMapping("/resources")
    @RequirePermission("system:resource:add")
    public Result<Void> saveResource(@RequestBody SysResource res) { permissionService.saveResource(res); return Result.ok(null); }

    @DeleteMapping("/resources/{id}")
    @RequirePermission("system:resource:del")
    public Result<Void> deleteResource(@PathVariable Long id) { permissionService.deleteResource(id); return Result.ok(null); }

    // ===== 角色授权 =====
    @GetMapping("/roles/{roleId}/resources")
    @RequirePermission("system:role:list")
    public Result<List<Long>> roleResources(@PathVariable Long roleId) { return Result.ok(permissionService.roleResourceIds(roleId)); }

    @PostMapping("/roles/{roleId}/resources")
    @RequirePermission("system:role:assign")
    public Result<Void> assignResources(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        permissionService.assignResources(roleId, body.get("resourceIds"));
        return Result.ok(null);
    }

    // ===== 用户 =====
    @GetMapping("/users")
    @RequirePermission("system:user:list")
    public Result<PageResult<SysUser>> users(@RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(permissionService.userPage(keyword, page, pageSize));
    }

    @GetMapping("/users/{userId}/roles")
    @RequirePermission("system:user:list")
    public Result<List<SysRole>> userRoles(@PathVariable Long userId) { return Result.ok(permissionService.userRoles(userId)); }

    @PostMapping("/users/{userId}/roles")
    @RequirePermission("system:user:assign")
    public Result<Void> assignUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> body) {
        permissionService.assignUserRoles(userId, body.get("roleIds"));
        return Result.ok(null);
    }
}
