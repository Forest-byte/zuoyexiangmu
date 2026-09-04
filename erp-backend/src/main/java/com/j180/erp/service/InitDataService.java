package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.Constants;
import com.j180.erp.entity.Employee;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.SysUser;
import com.j180.erp.entity.UserRole;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.SysUserMapper;
import com.j180.erp.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 系统初始化服务：应用启动后自动创建/修复内置账号与角色绑定
 * 内置账号初始密码统一为 Admin123456，首次登录后建议修改
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataService implements ApplicationRunner {

    /** 内置账号初始密码（满足密码策略：字母+数字至少8位） */
    public static final String DEFAULT_PASSWORD = "Admin123456";

    private final SysUserMapper sysUserMapper;
    private final RoleMapper roleMapper;
    private final EmployeeMapper employeeMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        try {
            initSuperAdminRole();
            initAccount(Constants.SUPER_ADMIN_USER, 1L, Constants.SUPER_ADMIN_ROLE, 1, "系统管理员");
            initRoleIfAbsent("HR", "人事专员", "负责员工档案维护（本部门数据权限）", 3, 1);
            initRoleIfAbsent("WH_MANAGER", "仓库主管", "负责仓库信息维护（本仓库数据权限）", 5, 2);
            initAccount("zhangsan", 2L, "HR", 3, "张三（人事专员）");
            initAccount("lisi", 3L, "WH_MANAGER", 5, "李四（仓库主管）");
            log.info("[InitDataService] 内置账号/角色初始化完成，默认密码: {}", DEFAULT_PASSWORD);
        } catch (Exception e) {
            log.error("[InitDataService] 内置数据初始化失败", e);
        }
    }

    /**
     * 确保超级管理员角色存在且为内置
     */
    private void initSuperAdminRole() {
        Role role = selectByCode(Constants.SUPER_ADMIN_ROLE);
        if (role == null) {
            role = new Role();
            role.setRoleCode(Constants.SUPER_ADMIN_ROLE);
            role.setRoleName("系统管理员");
            role.setDescription("内置超级管理员，默认拥有全部权限");
            role.setDataScope(1);
            role.setIsBuiltin(1);
            role.setStatus(Constants.STATUS_ENABLED);
            roleMapper.insert(role);
        } else {
            Role update = new Role();
            update.setId(role.getId());
            update.setIsBuiltin(1);
            roleMapper.updateById(update);
        }
    }

    /**
     * 确保演示角色存在
     */
    private void initRoleIfAbsent(String code, String name, String desc, int dataScope, int departmentId) {
        if (selectByCode(code) != null) {
            return;
        }
        Role role = new Role();
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setDescription(desc);
        role.setDataScope(dataScope);
        role.setDataScopeIds(String.valueOf(departmentId));
        role.setIsBuiltin(0);
        role.setStatus(Constants.STATUS_ENABLED);
        roleMapper.insert(role);
    }

    /**
     * 创建或修复内置/演示账号，并确保角色绑定
     */
    private void initAccount(String username, Long employeeId, String roleCode, int dataScope, String desc) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            log.warn("[InitDataService] 员工档案不存在，id={}，跳过账号 {}", employeeId, username);
            return;
        }
        SysUser user = sysUserMapper.selectByUsername(username);
        boolean created = user == null;
        if (created) {
            user = new SysUser();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            user.setEmployeeId(employeeId);
            user.setStatus(Constants.STATUS_ENABLED);
            user.setIsBuiltin(Constants.SUPER_ADMIN_USER.equals(username) ? 1 : 0);
            sysUserMapper.insert(user);
        } else {
            // 修复：确认员工绑定与内置标记；密码若非 BCrypt 哈希则重置
            boolean dirty = false;
            SysUser update = new SysUser();
            if (user.getIsBuiltin() == null || user.getIsBuiltin() != 1) {
                if (Constants.SUPER_ADMIN_USER.equals(username)) {
                    update.setIsBuiltin(1);
                    dirty = true;
                }
            }
            if (user.getEmployeeId() == null || !user.getEmployeeId().equals(employeeId)) {
                update.setEmployeeId(employeeId);
                dirty = true;
            }
            if (!StringUtils.hasText(user.getPasswordHash()) || !user.getPasswordHash().startsWith("$2")) {
                update.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
                dirty = true;
            }
            if (dirty) {
                update.setId(user.getId());
                sysUserMapper.updateById(update);
            }
        }
        bindRole(user.getId(), roleCode);
        if (employee.getUserId() == null || !employee.getUserId().equals(user.getId())) {
            Employee empUpdate = new Employee();
            empUpdate.setId(employeeId);
            empUpdate.setUserId(user.getId());
            employeeMapper.updateById(empUpdate);
        }
        log.info("[InitDataService] 内置账号 {} 初始化完成（{}）", username, created ? "新建" : "已存在且修复");
    }

    private void bindRole(Long userId, String roleCode) {
        Role role = selectByCode(roleCode);
        if (role == null) {
            log.warn("[InitDataService] 角色 {} 不存在，跳过绑定", roleCode);
            return;
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId).eq(UserRole::getRoleId, role.getId()));
        if (count == null || count == 0) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
    }

    private Role selectByCode(String roleCode) {
        return roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode).last("LIMIT 1"));
    }
}
