package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.BizException;
import com.j180.erp.common.Constants;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.LoginForm;
import com.j180.erp.dto.LoginResult;
import com.j180.erp.dto.PasswordForm;
import com.j180.erp.dto.ResourceTreeNode;
import com.j180.erp.dto.UserInfoVO;
import com.j180.erp.entity.Employee;
import com.j180.erp.entity.Resource;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.SysUser;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.mapper.ResourceMapper;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.SysUserMapper;
import com.j180.erp.security.JwtUtil;
import com.j180.erp.security.PermissionService;
import com.j180.erp.security.UserContext;
import com.j180.erp.security.UserContextHolder;
import com.j180.erp.validator.SysUserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务：登录/当前用户信息/菜单与权限构建/修改密码
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final EmployeeMapper employeeMapper;
    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final SysUserValidator sysUserValidator;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 登录
     */
    public LoginResult login(LoginForm form, HttpServletRequest request) {
        AssertUtil.notNull(form, "登录表单不能为空");
        AssertUtil.text(form.getUsername(), 64, "请输入登录名");
        AssertUtil.notBlank(form.getPassword(), "请输入密码");

        SysUser user = sysUserMapper.selectByUsername(form.getUsername());
        if (user == null || !passwordEncoder.matches(form.getPassword(), user.getPasswordHash())) {
            auditLogService.record((user != null ? user.getUsername() : form.getUsername()),
                    "认证中心", "登录失败", "用户名或密码错误", request);
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BizException("账号已停用，请联系管理员");
        }
        UserContext context = permissionService.buildContext(user.getId());
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), new ArrayList<>(context.getRoleIds()));
        auditLogService.record(user.getId(), user.getUsername(), "认证中心", "登录", "sys_user", user.getId(), null, null, request);

        LoginResult result = new LoginResult();
        result.setToken(token);
        result.setUser(buildUserInfo(user, context));
        return result;
    }

    /**
     * 获取当前登录用户信息（菜单树 + 功能权限 + 角色）
     */
    public UserInfoVO info() {
        UserContext context = UserContextHolder.getRequired();
        SysUser user = sysUserMapper.selectById(context.getUserId());
        AssertUtil.notNull(user, "账号不存在或已删除");
        return buildUserInfo(user, context);
    }

    /**
     * 修改当前用户密码
     */
    public void changePassword(PasswordForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        UserContext context = UserContextHolder.getRequired();
        SysUser user = sysUserMapper.selectById(context.getUserId());
        AssertUtil.notNull(user, "账号不存在");
        AssertUtil.notBlank(form.getOldPassword(), "请输入原密码");
        AssertUtil.isTrue(passwordEncoder.matches(form.getOldPassword(), user.getPasswordHash()), "原密码错误");
        sysUserValidator.validatePassword(form.getNewPassword());
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    /**
     * 构建当前登录用户信息
     */
    public UserInfoVO buildUserInfo(SysUser user, UserContext context) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmployeeId(context.getEmployeeId());
        vo.setSuperAdmin(context.isSuperAdmin());
        vo.setDataScope(context.getDataScope());
        vo.setLastLoginTime(user.getLastLoginTime());
        if (context.getEmployeeId() != null) {
            Employee employee = employeeMapper.selectById(context.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getName());
            }
        }
        // 角色
        List<Role> roles = roleMapper.selectEnabledByUserId(user.getId());
        vo.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        vo.setRoleNames(roles.stream().map(Role::getRoleName).collect(Collectors.toList()));
        // 功能权限（null 表示超级管理员拥有全部）
        if (context.isSuperAdmin()) {
            vo.setPermissions(null);
        } else if (context.getResCodes() != null) {
            vo.setPermissions(new ArrayList<>(context.getResCodes()));
        }
        // 菜单树（菜单 + 页面）
        vo.setMenus(buildMenuTree(context));
        return vo;
    }

    /**
     * 构建当前用户可见的菜单/页面树（父级未授权时子级自动上提为根）
     */
    private List<ResourceTreeNode> buildMenuTree(UserContext context) {
        List<Resource> resources = resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, Constants.STATUS_ENABLED)
                .in(Resource::getResType, Constants.RES_MENU, Constants.RES_PAGE)
                .orderByAsc(Resource::getSortNo).orderByAsc(Resource::getId));
        Set<String> owned = null;
        if (!context.isSuperAdmin()) {
            owned = context.getResCodes() == null ? Set.of() : context.getResCodes();
        }
        Map<Long, ResourceTreeNode> nodeMap = new LinkedHashMap<>();
        for (Resource r : resources) {
            if (owned != null && !owned.contains(r.getResCode())) {
                continue;
            }
            nodeMap.put(r.getId(), toTreeNode(r));
        }
        List<ResourceTreeNode> roots = new ArrayList<>();
        for (Map.Entry<Long, ResourceTreeNode> entry : nodeMap.entrySet()) {
            ResourceTreeNode node = entry.getValue();
            ResourceTreeNode parent = node.getParentId() == null ? null : nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private ResourceTreeNode toTreeNode(Resource r) {
        ResourceTreeNode node = new ResourceTreeNode();
        node.setId(r.getId());
        node.setResCode(r.getResCode());
        node.setResName(r.getResName());
        node.setResType(r.getResType());
        node.setParentId(r.getParentId());
        node.setPath(r.getPath());
        node.setHttpMethod(r.getHttpMethod());
        node.setSortNo(r.getSortNo());
        node.setIcon(r.getIcon());
        node.setStatus(r.getStatus());
        node.setChecked(false);
        return node;
    }
}
