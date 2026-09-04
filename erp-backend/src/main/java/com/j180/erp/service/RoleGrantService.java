package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.Constants;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.AssignUsersForm;
import com.j180.erp.dto.DataScopeForm;
import com.j180.erp.dto.RoleGrantForm;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.RoleResource;
import com.j180.erp.entity.SysUser;
import com.j180.erp.entity.UserRole;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.RoleResourceMapper;
import com.j180.erp.mapper.SysUserMapper;
import com.j180.erp.mapper.UserRoleMapper;
import com.j180.erp.security.PermissionCache;
import com.j180.erp.security.PermissionService;
import com.j180.erp.security.UserContextHolder;
import com.j180.erp.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色授权服务：功能权限授权 / 数据权限设置 / 角色-用户分配（角色权限模块）
 */
@Service
@RequiredArgsConstructor
public class RoleGrantService {

    private final RoleMapper roleMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final UserRoleMapper userRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final PermissionService permissionService;
    private final PermissionCache permissionCache;
    private final RoleValidator roleValidator;

    /**
     * 功能权限授权（覆盖式）；内置超级管理员权限只读，非超管不可越权授予自身没有的资源
     */
    @Transactional(rollbackFor = Exception.class)
    public void grant(RoleGrantForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notNull(form.getRoleId(), "角色ID不能为空");
        Role role = mustExist(form.getRoleId());
        AssertUtil.isFalse(isBuiltinSuper(role), "内置超级管理员为全量权限，不允许修改");
        List<Long> resourceIds = form.getResourceIds() == null ? Collections.emptyList() : form.getResourceIds();
        Set<Long> distinct = new LinkedHashSet<>(resourceIds);
        // 非超管防权限提升：只能授予自身已拥有的资源（R-MODEL-9）
        var operator = UserContextHolder.get();
        if (operator != null && !operator.isSuperAdmin()) {
            Set<Long> owned = permissionService.getOperatorResourceIds(operator);
            for (Long resourceId : distinct) {
                AssertUtil.isTrue(owned.contains(resourceId), "无权授予该资源，越权操作已拒绝");
            }
        }
        roleResourceMapper.delete(new LambdaQueryWrapper<RoleResource>()
                .eq(RoleResource::getRoleId, form.getRoleId()));
        for (Long resourceId : distinct) {
            RoleResource rr = new RoleResource();
            rr.setRoleId(form.getRoleId());
            rr.setResourceId(resourceId);
            roleResourceMapper.insert(rr);
        }
        permissionCache.evictRole(form.getRoleId());
    }

    /**
     * 数据权限设置；内置超级管理员数据范围固定为全部，不允许修改
     */
    public void dataScope(DataScopeForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notNull(form.getRoleId(), "角色ID不能为空");
        Role role = mustExist(form.getRoleId());
        AssertUtil.isFalse(isBuiltinSuper(role), "内置超级管理员数据范围固定为全部，不允许修改");
        roleValidator.validateDataScope(form.getDataScope(), form.getDataScopeIds());
        role.setDataScope(form.getDataScope());
        role.setDataScopeIds(form.getDataScopeIds());
        roleMapper.updateById(role);
    }

    /**
     * 角色-用户分配（覆盖式）；内置超级管理员角色仅超级管理员可维护
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(AssignUsersForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notNull(form.getRoleId(), "角色ID不能为空");
        Role role = mustExist(form.getRoleId());
        boolean superRole = isBuiltinSuper(role);
        var operator = UserContextHolder.get();
        AssertUtil.isFalse(superRole && operator != null && !operator.isSuperAdmin(),
                "内置超级管理员角色仅超级管理员可维护");
        List<Long> userIds = form.getUserIds() == null ? Collections.emptyList() : form.getUserIds();
        Set<Long> distinct = new LinkedHashSet<>(userIds);
        for (Long userId : distinct) {
            SysUser user = sysUserMapper.selectById(userId);
            AssertUtil.notNull(user, "账号不存在或已删除");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, form.getRoleId()));
        for (Long userId : distinct) {
            UserRole ur = new UserRole();
            ur.setRoleId(form.getRoleId());
            ur.setUserId(userId);
            userRoleMapper.insert(ur);
        }
    }

    private boolean isBuiltinSuper(Role role) {
        return role.getIsBuiltin() != null && role.getIsBuiltin() == 1
                && Constants.SUPER_ADMIN_ROLE.equals(role.getRoleCode());
    }

    private Role mustExist(Long id) {
        Role role = roleMapper.selectById(id);
        AssertUtil.notNull(role, "角色不存在或已删除");
        return role;
    }
}
