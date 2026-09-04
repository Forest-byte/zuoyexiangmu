package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j180.erp.common.Constants;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.enums.DataScopeEnum;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.RoleForm;
import com.j180.erp.dto.RoleQuery;
import com.j180.erp.dto.RoleVO;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.RoleResource;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.RoleResourceMapper;
import com.j180.erp.security.PermissionCache;
import com.j180.erp.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色维护服务（RBAC 核心实体）
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final PermissionCache permissionCache;
    private final RoleValidator roleValidator;

    public PageResult<RoleVO> page(RoleQuery query) {
        AssertUtil.notNull(query, "查询条件不能为空");
        query.validatePaging();
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Role::getRoleCode, keyword).or().like(Role::getRoleName, keyword));
        }
        wrapper.eq(query.getStatus() != null, Role::getStatus, query.getStatus());
        wrapper.orderByAsc(Role::getId);
        Page<Role> page = roleMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page, this::toVO);
    }

    public List<Role> listEnabled() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, Constants.STATUS_ENABLED)
                .orderByAsc(Role::getId));
    }

    public RoleVO getVO(Long id) {
        return toVO(mustExist(id));
    }

    public Role getById(Long id) {
        return mustExist(id);
    }

    public void create(RoleForm form) {
        roleValidator.validate(form);
        AssertUtil.isNull(selectByCode(form.getRoleCode()), "角色编码已存在");
        AssertUtil.isNull(selectByName(form.getRoleName()), "角色名称已存在");
        Role role = new Role();
        BeanUtils.copyProperties(form, role);
        if (role.getDataScope() == null) {
            role.setDataScope(DataScopeEnum.SELF.getCode());
        }
        if (role.getStatus() == null) {
            role.setStatus(Constants.STATUS_ENABLED);
        }
        role.setIsBuiltin(0);
        roleMapper.insert(role);
    }

    public void update(RoleForm form) {
        AssertUtil.notNull(form.getId(), "角色ID不能为空");
        roleValidator.validate(form);
        Role role = mustExist(form.getId());
        boolean builtin = role.getIsBuiltin() != null && role.getIsBuiltin() == 1;
        if (builtin) {
            // 内置角色编码锁定
            AssertUtil.isTrue(Constants.SUPER_ADMIN_ROLE.equals(role.getRoleCode())
                            || Constants.SUPER_ADMIN_ROLE.equals(form.getRoleCode()),
                    "内置角色编码不允许修改");
            AssertUtil.isTrue(role.getRoleCode().equals(form.getRoleCode()), "内置角色编码不允许修改");
        }
        AssertUtil.isFalse(selectByCode(form.getRoleCode()) != null
                        && !selectByCode(form.getRoleCode()).getId().equals(role.getId()),
                "角色编码已存在");
        AssertUtil.isFalse(selectByName(form.getRoleName()) != null
                        && !selectByName(form.getRoleName()).getId().equals(role.getId()),
                "角色名称已存在");
        BeanUtils.copyProperties(form, role, "id", "isBuiltin", "createTime", "updateTime");
        roleMapper.updateById(role);
    }

    public void updateStatus(Long id, StatusForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        StatusEnum.check(form.getStatus());
        Role role = mustExist(id);
        boolean builtin = role.getIsBuiltin() != null && role.getIsBuiltin() == 1;
        AssertUtil.isFalse(builtin && form.getStatus() == Constants.STATUS_DISABLED, "内置角色不可停用");
        role.setStatus(form.getStatus());
        roleMapper.updateById(role);
        permissionCache.evictAll();
    }

    /**
     * 复制角色：基本信息 + 数据范围 + 功能权限逐一复制（内置超级管理员不可复制）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long sourceId, RoleForm form) {
        AssertUtil.notNull(sourceId, "源角色ID不能为空");
        Role source = mustExist(sourceId);
        AssertUtil.isFalse(source.getIsBuiltin() != null && source.getIsBuiltin() == 1,
                "内置超级管理员角色不允许复制");
        roleValidator.validate(form);
        AssertUtil.isNull(selectByCode(form.getRoleCode()), "角色编码已存在");
        AssertUtil.isNull(selectByName(form.getRoleName()), "角色名称已存在");
        Role role = new Role();
        BeanUtils.copyProperties(form, role);
        role.setDataScope(source.getDataScope());
        role.setDataScopeIds(source.getDataScopeIds());
        role.setIsBuiltin(0);
        role.setStatus(Constants.STATUS_ENABLED);
        roleMapper.insert(role);
        for (Long resourceId : roleResourceMapper.selectResourceIdsByRoleId(sourceId)) {
            RoleResource rr = new RoleResource();
            rr.setRoleId(role.getId());
            rr.setResourceId(resourceId);
            roleResourceMapper.insert(rr);
        }
        permissionCache.evictRole(role.getId());
        return role.getId();
    }

    public void delete(Long id) {
        Role role = mustExist(id);
        AssertUtil.isFalse(role.getIsBuiltin() != null && role.getIsBuiltin() == 1, "内置角色不可删除");
        long userCount = roleMapper.countUsersByRoleId(id);
        AssertUtil.isTrue(userCount == 0, "角色下仍有关联用户，不可删除，请先在用户账号中移除");
        roleResourceMapper.delete(new LambdaQueryWrapper<RoleResource>().eq(RoleResource::getRoleId, id));
        roleMapper.deleteById(id);
        permissionCache.evictRole(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    private Role mustExist(Long id) {
        Role role = roleMapper.selectById(id);
        AssertUtil.notNull(role, "角色不存在或已删除");
        return role;
    }

    private Role selectByCode(String code) {
        return roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, code).last("LIMIT 1"));
    }

    private Role selectByName(String name) {
        return roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, name).last("LIMIT 1"));
    }

    private List<RoleVO> toVOList(List<Role> roles) {
        return roles.stream().map(this::toVO).collect(Collectors.toList());
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        vo.setUserCount(roleMapper.countUsersByRoleId(role.getId()));
        List<Long> resourceIds = roleResourceMapper.selectResourceIdsByRoleId(role.getId());
        vo.setResourceCount((long) resourceIds.size());
        vo.setResourceIds(resourceIds);
        vo.setDataScopeLabel(DataScopeEnum.of(role.getDataScope()).getLabel());
        return vo;
    }
}
