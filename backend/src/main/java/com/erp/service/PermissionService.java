package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.SysResource;
import com.erp.entity.SysRole;
import com.erp.entity.SysUser;
import com.erp.mapper.ResourceMapper;
import com.erp.mapper.RoleMapper;
import com.erp.mapper.UserMapper;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 权限服务：角色维护/资源维护/用户角色授权
 */
@Service
public class PermissionService {

    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public PermissionService(RoleMapper roleMapper, ResourceMapper resourceMapper, UserMapper userMapper, AuditService auditService) {
        this.roleMapper = roleMapper;
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    // ============ 角色 ============
    public List<SysRole> roles() { return roleMapper.selectAll(); }

    @Transactional
    public void saveRole(SysRole role) {
        if (role.getId() == null) {
            role.setCreateBy(UserContext.currentName());
            roleMapper.insert(role);
            auditService.log("新增角色", role.getName(), "", "");
        } else {
            roleMapper.update(role);
            auditService.log("编辑角色", role.getName(), "", "");
        }
    }

    @Transactional
    public void deleteRole(Long id) {
        SysRole role = roleMapper.findById(id);
        if (role != null && "ROLE_ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("内置管理员角色禁止删除");
        }
        if (roleMapper.countUserByRole(id) > 0) {
            throw new BusinessException("角色已被用户引用，禁止删除");
        }
        roleMapper.delete(id);
        auditService.log("删除角色", String.valueOf(id), "", "");
    }

    // ============ 资源 ============
    public List<SysResource> resourceTree() {
        List<SysResource> all = resourceMapper.selectAll();
        Map<Long, List<SysResource>> byParent = new LinkedHashMap<>();
        for (SysResource r : all) byParent.computeIfAbsent(r.getParentId() == null ? 0L : r.getParentId(), k -> new ArrayList<>()).add(r);
        for (List<SysResource> list : byParent.values()) {
            list.sort(Comparator.comparing(r -> r.getSort() == null ? 0 : r.getSort()));
        }
        List<SysResource> roots = new ArrayList<>();
        for (SysResource r : byParent.getOrDefault(0L, new ArrayList<>())) {
            fill(r, byParent);
            roots.add(r);
        }
        return roots;
    }

    private void fill(SysResource p, Map<Long, List<SysResource>> byParent) {
        List<SysResource> children = byParent.getOrDefault(p.getId(), new ArrayList<>());
        for (SysResource c : children) fill(c, byParent);
        p.setChildren(children);
    }

    @Transactional
    public void saveResource(SysResource res) {
        if (res.getId() == null) {
            res.setCreateBy(UserContext.currentName());
            resourceMapper.insert(res);
        } else {
            resourceMapper.update(res);
        }
    }

    @Transactional
    public void deleteResource(Long id) {
        if (resourceMapper.countChildren(id) > 0) throw new BusinessException("存在子资源，禁止删除");
        resourceMapper.delete(id);
    }

    // ============ 角色授权 ============
    @Transactional
    public void assignResources(Long roleId, List<Long> resourceIds) {
        roleMapper.deleteRoleResources(roleId);
        if (resourceIds != null) {
            for (Long rid : resourceIds) {
                roleMapper.insertRoleResource(roleId, rid);
            }
        }
        SysRole role = roleMapper.findById(roleId);
        auditService.log("角色授权", role == null ? String.valueOf(roleId) : role.getName(), "", resourceIds == null ? "[]" : resourceIds.toString());
    }

    public List<Long> roleResourceIds(Long roleId) { return roleMapper.selectResourceIds(roleId); }

    // ============ 用户角色授权 ============
    public List<SysUser> users(String keyword, int page, int pageSize) {
        return userMapper.page(keyword, (page - 1) * pageSize, pageSize);
    }

    public long userCount(String keyword) { return userMapper.count(keyword); }

    public List<SysRole> userRoles(Long userId) { return roleMapper.selectByUserId(userId); }

    public PageResult<SysUser> userPage(String keyword, int page, int pageSize) {
        return PageResult.of(userMapper.count(keyword), userMapper.page(keyword, (page - 1) * pageSize, pageSize));
    }

    @Transactional
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        userMapper.deleteUserRoles(userId);
        if (roleIds != null) {
            for (Long rid : roleIds) {
                userMapper.insertUserRole(userId, rid);
            }
        }
        SysUser u = userMapper.findById(userId);
        auditService.log("用户角色授权", u == null ? String.valueOf(userId) : u.getUsername(), "", roleIds == null ? "[]" : roleIds.toString());
    }
}
