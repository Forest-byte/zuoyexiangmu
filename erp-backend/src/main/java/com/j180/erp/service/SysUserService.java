package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j180.erp.common.Constants;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.AssignRolesForm;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.ResetPasswordForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.dto.UserForm;
import com.j180.erp.dto.UserQuery;
import com.j180.erp.dto.UserVO;
import com.j180.erp.entity.Department;
import com.j180.erp.entity.Employee;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.SysUser;
import com.j180.erp.entity.UserRole;
import com.j180.erp.mapper.DepartmentMapper;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.SysUserMapper;
import com.j180.erp.mapper.UserRoleMapper;
import com.j180.erp.security.PermissionCache;
import com.j180.erp.security.UserContext;
import com.j180.erp.security.UserContextHolder;
import com.j180.erp.validator.SysUserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户账号服务（角色权限-用户账号模块）
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserValidator sysUserValidator;
    private final PermissionCache permissionCache;

    public PageResult<UserVO> page(UserQuery query) {
        AssertUtil.notNull(query, "查询条件不能为空");
        query.validatePaging();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().apply("employee_id IN (SELECT id FROM sys_employee WHERE name LIKE {0})", "%" + keyword + "%"));
        }
        wrapper.eq(query.getStatus() != null, SysUser::getStatus, query.getStatus());
        if (query.getRoleId() != null) {
            wrapper.apply("id IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", query.getRoleId());
        }
        wrapper.orderByAsc(SysUser::getId);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page, this::toVO);
    }

    public UserVO getVO(Long id) {
        return toVO(mustExist(id));
    }

    public void create(UserForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        sysUserValidator.validate(form, true);
        AssertUtil.isNull(sysUserMapper.selectByUsername(form.getUsername()), "登录名已存在");
        checkEmployeeAvailable(form.getEmployeeId(), null);
        SysUser user = new SysUser();
        user.setUsername(form.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setEmployeeId(form.getEmployeeId());
        user.setStatus(form.getStatus() == null ? Constants.STATUS_ENABLED : form.getStatus());
        user.setIsBuiltin(0);
        sysUserMapper.insert(user);
    }

    public void update(UserForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notNull(form.getId(), "用户ID不能为空");
        sysUserValidator.validate(form, false);
        SysUser user = mustExist(form.getId());
        boolean builtin = user.getIsBuiltin() != null && user.getIsBuiltin() == 1;
        if (builtin) {
            AssertUtil.isTrue(user.getUsername().equals(form.getUsername()), "内置账号登录名不允许修改");
        } else {
            SysUser exist = sysUserMapper.selectByUsername(form.getUsername());
            AssertUtil.isFalse(exist != null && !exist.getId().equals(user.getId()), "登录名已存在");
        }
        if (form.getEmployeeId() != null && !form.getEmployeeId().equals(user.getEmployeeId())) {
            checkEmployeeAvailable(form.getEmployeeId(), user.getId());
        }
        user.setUsername(form.getUsername().trim());
        user.setEmployeeId(form.getEmployeeId());
        if (StringUtils.hasText(form.getPassword())) {
            sysUserValidator.validatePassword(form.getPassword());
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        if (form.getStatus() != null) {
            if (builtin && form.getStatus() == Constants.STATUS_DISABLED) {
                AssertUtil.isTrue(false, "内置账号不可停用");
            }
            user.setStatus(form.getStatus());
        }
        sysUserMapper.updateById(user);
    }

    public void updateStatus(Long id, StatusForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        StatusEnum.check(form.getStatus());
        SysUser user = mustExist(id);
        AssertUtil.isFalse(user.getIsBuiltin() != null && user.getIsBuiltin() == 1
                        && form.getStatus() == Constants.STATUS_DISABLED,
                "内置账号不可停用");
        user.setStatus(form.getStatus());
        sysUserMapper.updateById(user);
    }

    public void resetPassword(Long id, ResetPasswordForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        sysUserValidator.validatePassword(form.getNewPassword());
        SysUser user = mustExist(id);
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    /**
     * 为账号分配角色（覆盖式，R-MODEL-3）；内置账号角色由系统维护不允许修改
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(AssignRolesForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notNull(form.getUserId(), "用户ID不能为空");
        SysUser user = mustExist(form.getUserId());
        AssertUtil.isFalse(user.getIsBuiltin() != null && user.getIsBuiltin() == 1, "内置账号角色由系统维护，不允许修改");
        List<Long> roleIds = form.getRoleIds() == null ? Collections.emptyList() : form.getRoleIds();
        Set<Long> distinct = new LinkedHashSet<>(roleIds);
        for (Long roleId : distinct) {
            Role role = roleMapper.selectById(roleId);
            AssertUtil.notNull(role, "角色不存在或已删除");
            AssertUtil.isTrue(role.getStatus() == Constants.STATUS_ENABLED, "角色已停用，无法分配");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, form.getUserId()));
        for (Long roleId : distinct) {
            UserRole ur = new UserRole();
            ur.setUserId(form.getUserId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    public void delete(Long id) {
        SysUser user = mustExist(id);
        AssertUtil.isTrue(user.getIsBuiltin() == null || user.getIsBuiltin() != 1, "内置账号不可删除");
        UserContext context = UserContextHolder.get();
        if (context != null && context.getUserId().equals(id)) {
            AssertUtil.isTrue(false, "不能删除当前登录账号");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        sysUserMapper.deleteById(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    private void checkEmployeeAvailable(Long employeeId, Long excludeUserId) {
        if (employeeId == null) {
            return;
        }
        Employee employee = employeeMapper.selectById(employeeId);
        AssertUtil.notNull(employee, "关联员工不存在");
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeId, employeeId);
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        AssertUtil.isTrue(sysUserMapper.selectCount(wrapper) == 0, "该员工已关联其他账号（一人一账号）");
    }

    private List<UserVO> toVOList(List<SysUser> users) {
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        Map<Long, Employee> empMap = new HashMap<>();
        Map<Long, Department> deptMap = new HashMap<>();
        Set<Long> empIds = new LinkedHashSet<>();
        Set<Long> deptIds = new LinkedHashSet<>();
        for (SysUser u : users) {
            if (u.getEmployeeId() != null) {
                empIds.add(u.getEmployeeId());
            }
        }
        if (!empIds.isEmpty()) {
            for (Employee e : employeeMapper.selectBatchIds(empIds)) {
                empMap.put(e.getId(), e);
                if (e.getDepartmentId() != null) {
                    deptIds.add(e.getDepartmentId());
                }
            }
        }
        if (!deptIds.isEmpty()) {
            for (Department d : departmentMapper.selectBatchIds(deptIds)) {
                deptMap.put(d.getId(), d);
            }
        }
        List<UserVO> result = new ArrayList<>();
        for (SysUser user : users) {
            UserVO vo = UserVO.from(user);
            Employee emp = user.getEmployeeId() == null ? null : empMap.get(user.getEmployeeId());
            if (emp != null) {
                vo.setEmployeeName(emp.getName());
                Department dept = emp.getDepartmentId() == null ? null : deptMap.get(emp.getDepartmentId());
                vo.setDepartmentName(dept == null ? null : dept.getDeptName());
            }
            List<Role> roles = roleMapper.selectEnabledByUserId(user.getId());
            vo.setRoleIds(roles.stream().map(Role::getId).toList());
            vo.setRoleNames(roles.stream().map(Role::getRoleName).toList());
            result.add(vo);
        }
        return result;
    }

    private UserVO toVO(SysUser user) {
        List<UserVO> list = toVOList(Collections.singletonList(user));
        return list.isEmpty() ? UserVO.from(user) : list.get(0);
    }

    private SysUser mustExist(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        AssertUtil.notNull(user, "账号不存在或已删除");
        return user;
    }
}
