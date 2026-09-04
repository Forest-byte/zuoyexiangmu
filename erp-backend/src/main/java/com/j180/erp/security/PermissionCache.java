package com.j180.erp.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.Constants;
import com.j180.erp.entity.Resource;
import com.j180.erp.entity.Role;
import com.j180.erp.entity.RoleResource;
import com.j180.erp.mapper.ResourceMapper;
import com.j180.erp.mapper.RoleMapper;
import com.j180.erp.mapper.RoleResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 角色-资源权限缓存
 * <p>
 * 代替 Redis 实现 perm:role:{roleId} 缓存语义（单机部署；集群部署时可无缝替换为 Redis 实现）：
 * 未命中回源数据库重建并回填；角色授权/资源停用/角色停用等变更后按角色失效缓存，
 * 下一次请求自动回源重建，从而实现"授权即时生效"。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionCache {

    private final RoleMapper roleMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final ResourceMapper resourceMapper;

    /** roleId -> 该角色可访问的资源编码集合 */
    private final Map<Long, Set<String>> cache = new ConcurrentHashMap<>();

    /**
     * 获取角色可访问的资源编码集合（停用角色返回空集合）
     */
    public Set<String> getResCodes(Long roleId) {
        return cache.computeIfAbsent(roleId, this::loadFromDb);
    }

    private Set<String> loadFromDb(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getStatus() == Constants.STATUS_DISABLED) {
            return Collections.emptySet();
        }
        // 仅保留启用状态资源的编码（R-RP-02-4 已授权资源被停用后自动失效）
        List<Long> resourceIds = roleResourceMapper.selectList(
                        new LambdaQueryWrapper<RoleResource>().eq(RoleResource::getRoleId, roleId))
                .stream().map(RoleResource::getResourceId).collect(Collectors.toList());
        if (resourceIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                        .in(Resource::getId, resourceIds)
                        .eq(Resource::getStatus, Constants.STATUS_ENABLED))
                .stream().map(Resource::getResCode).collect(Collectors.toSet()));
    }

    /**
     * 获取角色可访问的资源ID集合（启用资源），用于防权限提升校验
     */
    public Set<Long> getResourceIds(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getStatus() == Constants.STATUS_DISABLED) {
            return Collections.emptySet();
        }
        List<Long> resourceIds = roleResourceMapper.selectList(
                        new LambdaQueryWrapper<RoleResource>().eq(RoleResource::getRoleId, roleId))
                .stream().map(RoleResource::getResourceId).collect(Collectors.toList());
        if (resourceIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                        .in(Resource::getId, resourceIds)
                        .eq(Resource::getStatus, Constants.STATUS_ENABLED)
                        .select(Resource::getId))
                .stream().map(Resource::getId).collect(Collectors.toSet()));
    }

    /**
     * 按角色失效缓存（角色授权变更、角色停用/删除后调用）
     */
    public void evictRole(Long roleId) {
        if (roleId != null) {
            cache.remove(roleId);
            log.info("权限缓存已失效: perm:role:{}", roleId);
        }
    }

    /**
     * 全量失效（资源状态变更等影响多个角色的场景）
     */
    public void evictAll() {
        cache.clear();
        log.info("权限缓存已全量失效");
    }
}
