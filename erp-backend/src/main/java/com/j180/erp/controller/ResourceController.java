package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.Result;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.ResourceForm;
import com.j180.erp.dto.ResourceQuery;
import com.j180.erp.dto.ResourceTreeNode;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Resource;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.ResourceService;
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
 * 资源维护接口（菜单/页面/按钮/接口树）
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/tree")
    @RequiresPermission("P_RESOURCE")
    public Result<List<ResourceTreeNode>> tree(ResourceQuery query) {
        return Result.ok(resourceService.tree(query));
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_RESOURCE")
    public Result<Resource> detail(@PathVariable Long id) {
        return Result.ok(resourceService.getById(id));
    }

    @PostMapping
    @RequiresPermission("B_RES_ADD")
    @Audit(module = "资源维护", action = "新增资源", targetType = "sys_resource")
    public Result<Void> create(@RequestBody ResourceForm form) {
        resourceService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_RES_EDIT")
    @Audit(module = "资源维护", action = "编辑资源", targetType = "sys_resource")
    public Result<Void> update(@RequestBody ResourceForm form) {
        resourceService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("B_RES_STATUS")
    @Audit(module = "资源维护", action = "停用启用资源", targetType = "sys_resource")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusForm form) {
        resourceService.updateStatus(id, form);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_RES_DELETE")
    @Audit(module = "资源维护", action = "删除资源", targetType = "sys_resource")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_RES_DELETE")
    @Audit(module = "资源维护", action = "批量删除资源", targetType = "sys_resource")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        resourceService.deleteBatch(form);
        return Result.ok();
    }
}
