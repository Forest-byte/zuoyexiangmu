package com.j180.erp.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.BizException;
import com.j180.erp.common.Constants;
import com.j180.erp.common.Result;
import com.j180.erp.common.enums.DataScopeEnum;
import com.j180.erp.entity.Employee;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.SysUser;
import com.j180.erp.mapper.DepartmentMapper;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.mapper.EmployeeWarehouseMapper;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 鉴权服务：构建当前用户上下文（角色/功能权限/数据权限）
 * <p>
 * 用户-角色关系每次请求实时读取数据库（不从JWT读取），保证移除角色/停用角色即时生效；
 * 角色-资源映射走 PermissionCache 缓存，缓存未命中回源重建。
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysUserMapper sysUserMapper;
    private final RoleMapper roleMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final EmployeeWarehouseMapper employeeWarehouseMapper;
    private final PermissionCache permissionCache;

    /**
     * 根据用户ID构建请求级用户上下文
     */
    public UserContext buildContext(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(Result.UNAUTHORIZED, "账号不存在或已删除");
        }
        if (user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BizException(Result.UNAUTHORIZED, "账号已停用，请联系管理员");
        }

        Employee employee = null;
        if (user.getEmployeeId() != null) {
            employee = employeeMapper.selectById(user.getEmployeeId());
        }

        // 实时读取用户的启用角色
        List<Role> roles = roleMapper.selectEnabledByUserId(userId);
        boolean superAdmin = roles.stream().anyMatch(r -> Constants.SUPER_ADMIN_ROLE.equals(r.getRoleCode()));

        UserContext.UserContextBuilder builder = UserContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .employeeId(employee != null ? employee.getId() : null)
                .departmentId(employee != null ? employee.getDepartmentId() : null)
                .roleIds(roles.stream().map(Role::getId).collect(Collectors.toSet()))
                .superAdmin(superAdmin);

        if (superAdmin) {
            return builder
                    .dataScope(DataScopeEnum.ALL.getCode())
                    .deptScopeIds(Collections.emptySet())
                    .warehouseScopeIds(Collections.emptySet())
                    .resCodes(null)
                    .build();
        }

        // 功能权限：多角色取并集（R-MODEL-3）
        Set<String> resCodes = new HashSet<>();
        for (Role role : roles) {
            resCodes.addAll(permissionCache.getResCodes(role.getId()));
        }

        // 数据权限：多角色取最宽（R-MODEL-4）
        Role widest = null;
        for (Role role : roles) {
            if (widest == null || DataScopeEnum.of(role.getDataScope()).getRank()
                    > DataScopeEnum.of(widest.getDataScope()).getRank()) {
                widest = role;
            }
        }

        if (widest == null) {
            // 无任何角色：仅保留登录能力，无任何功能权限，数据范围按最窄处理
            return builder.dataScope(DataScopeEnum.SELF.getCode())
                    .deptScopeIds(Collections.emptySet())
                    .warehouseScopeIds(Collections.emptySet())
                    .resCodes(resCodes)
                    .build();
        }

        DataScopeEnum scope = DataScopeEnum.of(widest.getDataScope());
        builder.dataScope(scope.getCode())
                .resCodes(resCodes)
                .deptScopeIds(resolveDeptScopeIds(scope, widest, employee))
                .warehouseScopeIds(resolveWarehouseScopeIds(scope, widest, employee));
        return builder.build();
    }

    /**
     * 解析部门类数据范围ID集合：
     * 角色配置了明细则用明细（范围2时展开子部门），否则按当前用户所在部门推导（PRD 6.5.3）
     */
    private Set<Long> resolveDeptScopeIds(DataScopeEnum scope, Role role, Employee employee) {
        if (scope != DataScopeEnum.DEPT_AND_CHILD && scope != DataScopeEnum.DEPT) {
            return Collections.emptySet();
        }
        List<Long> baseIds = new ArrayList<>();
        if (StringUtils.hasText(role.getDataScopeIds())) {
            for (String s : role.getDataScopeIds().split(",")) {
                if (StringUtils.hasText(s)) {
                    baseIds.add(Long.parseLong(s.trim()));
                }
            }
        } else if (employee != null && employee.getDepartmentId() != null) {
            baseIds.add(employee.getDepartmentId());
        }
        if (CollectionUtils.isEmpty(baseIds)) {
            return Collections.emptySet();
        }
        Set<Long> result = new HashSet<>(baseIds);
        if (scope == DataScopeEnum.DEPT_AND_CHILD) {
            for (Long deptId : baseIds) {
                result.addAll(departmentMapper.selectSubtreeIds(deptId));
            }
        }
        return result;
    }

    /**
     * 解析本仓库数据范围仓库ID集合：角色配置了明细则用明细，否则取当前员工的仓库绑定
     */
    private Set<Long> resolveWarehouseScopeIds(DataScopeEnum scope, Role role, Employee employee) {
        if (scope != DataScopeEnum.WAREHOUSE) {
            return Collections.emptySet();
        }
        if (StringUtils.hasText(role.getDataScopeIds())) {
            Set<Long> ids = new HashSet<>();
            for (String s : role.getDataScopeIds().split(",")) {
                if (StringUtils.hasText(s)) {
                    ids.add(Long.parseLong(s.trim()));
                }
            }
            return ids;
        }
        if (employee == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(employeeWarehouseMapper.selectWarehouseIdsByEmployeeId(employee.getId()));
    }

    /**
     * 获取操作者当前拥有的资源ID集合（用于防权限提升校验 R-MODEL-9）
     */
    public Set<Long> getOperatorResourceIds(UserContext context) {
        if (context.isSuperAdmin()) {
            return null;
        }
        return context.getRoleIds().stream()
                .flatMap(roleId -> permissionCache.getResourceIds(roleId).stream())
                .collect(Collectors.toSet());
    }
}
