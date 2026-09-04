package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.entity.SysRole;
import com.erp.entity.SysResource;
import com.erp.entity.SysUser;
import com.erp.mapper.ResourceMapper;
import com.erp.mapper.RoleMapper;
import com.erp.mapper.UserMapper;
import com.erp.model.LoginUser;
import com.erp.util.JwtUtil;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 认证与权限服务：登录 / 用户信息 / 改密
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper, ResourceMapper resourceMapper,
                       JwtUtil jwtUtil, AuditService auditService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.resourceMapper = resourceMapper;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
    }

    /** 登录 */
    @Transactional
    public Map<String, Object> login(String username, String password) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        LoginUser lu = buildLoginUser(user);
        String token = jwtUtil.generate(lu);
        auditService.log("登录系统", username, "", "");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> u = new HashMap<>();
        u.put("id", user.getId());
        u.put("username", user.getUsername());
        u.put("name", user.getName());
        u.put("roleCode", user.getRoleCode());
        u.put("roles", lu.getRoles());
        data.put("user", u);
        return data;
    }

    /** 当前用户信息 + 菜单 + 权限 */
    public Map<String, Object> info(Long userId) {
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LoginUser lu = buildLoginUser(user);
        List<SysResource> menus = loadMenus(lu.getIsAdmin() != null && lu.getIsAdmin(), userMapper.selectRoleIds(userId));
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> u = new HashMap<>();
        u.put("id", user.getId());
        u.put("username", user.getUsername());
        u.put("name", user.getName());
        u.put("roleCode", user.getRoleCode());
        data.put("user", u);
        data.put("menus", buildMenuTree(menus));
        data.put("permissions", lu.getPermissions());
        return data;
    }

    /** 修改密码：校验旧密码，新密码强度 ≥6 位含字母数字 */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new BusinessException("旧密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码至少 6 位");
        }
        if (!newPassword.matches(".*[a-zA-Z].*") || !newPassword.matches(".*\\d.*")) {
            throw new BusinessException("新密码需同时包含字母和数字");
        }
        userMapper.updatePassword(userId, newPassword);
        auditService.log("修改密码", user.getUsername(), "", "");
    }

    /** 按用户动态加载权限码集合（admin 返回 null 表示全权限） */
    public Set<String> permissionsOf(Long userId) {
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            return Collections.emptySet();
        }
        boolean admin = "ROLE_ADMIN".equals(user.getRoleCode());
        if (admin) {
            return null;
        }
        LoginUser lu = buildLoginUser(user);
        return new HashSet<>(lu.getPermissions());
    }

    /** 构建 LoginUser（含角色与权限码） */
    public LoginUser buildLoginUser(SysUser user) {        LoginUser lu = new LoginUser();
        lu.setUserId(user.getId());
        lu.setUsername(user.getUsername());
        lu.setName(user.getName());
        lu.setRoleCode(user.getRoleCode());
        boolean admin = "ROLE_ADMIN".equals(user.getRoleCode());
        lu.setIsAdmin(admin);
        List<Long> roleIds = userMapper.selectRoleIds(user.getId());
        List<String> roles = new ArrayList<>();
        List<String> perms = new ArrayList<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long rid : roleIds) {
                SysRole r = roleMapper.findById(rid);
                if (r != null) roles.add(r.getRoleCode());
                if (!admin) {
                    List<String> codes = resourceMapper.selectPermCodesByRole(rid);
                    if (codes != null) perms.addAll(codes);
                }
            }
        }
        if (admin) {
            roles.add("ROLE_ADMIN");
            // admin 拥有全部权限
            for (SysResource res : resourceMapper.selectAll()) {
                if (res.getCode() != null) perms.add(res.getCode());
            }
        }
        lu.setRoles(roles);
        lu.setPermissions(perms);
        return lu;
    }

    /** 加载菜单（menu 类型资源，按角色授权过滤） */
    private List<SysResource> loadMenus(boolean admin, List<Long> roleIds) {
        List<SysResource> all = resourceMapper.selectAll();
        if (admin) return all;
        Set<Long> allowed = new HashSet<>();
        if (roleIds != null) {
            for (Long rid : roleIds) {
                List<Long> ids = roleMapper.selectResourceIds(rid);
                if (ids != null) allowed.addAll(ids);
            }
        }
        List<SysResource> filtered = new ArrayList<>();
        for (SysResource r : all) {
            if (allowed.contains(r.getId())) filtered.add(r);
        }
        return filtered;
    }

    /** 构建菜单树（L1->L2->L3，仅 menu 类型） */
    public List<SysResource> buildMenuTree(List<SysResource> all) {
        Map<Long, List<SysResource>> byParent = new LinkedHashMap<>();
        for (SysResource r : all) {
            if (!"menu".equals(r.getType())) continue;
            byParent.computeIfAbsent(r.getParentId() == null ? 0L : r.getParentId(), k -> new ArrayList<>()).add(r);
        }
        for (List<SysResource> list : byParent.values()) {
            list.sort(Comparator.comparing(r -> r.getSort() == null ? 0 : r.getSort()));
        }
        List<SysResource> roots = new ArrayList<>();
        for (SysResource r : byParent.getOrDefault(0L, new ArrayList<>())) {
            fillChildren(r, byParent);
            roots.add(r);
        }
        return roots;
    }

    private void fillChildren(SysResource parent, Map<Long, List<SysResource>> byParent) {
        List<SysResource> children = byParent.getOrDefault(parent.getId(), new ArrayList<>());
        for (SysResource c : children) {
            fillChildren(c, byParent);
        }
        parent.setChildren(children);
    }
}
