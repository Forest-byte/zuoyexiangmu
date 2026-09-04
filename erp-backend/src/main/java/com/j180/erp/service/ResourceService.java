package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.Constants;
import com.j180.erp.common.enums.ResourceTypeEnum;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.ResourceForm;
import com.j180.erp.dto.ResourceQuery;
import com.j180.erp.dto.ResourceTreeNode;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Resource;
import com.j180.erp.entity.RoleResource;
import com.j180.erp.mapper.ResourceMapper;
import com.j180.erp.mapper.RoleResourceMapper;
import com.j180.erp.security.PermissionCache;
import com.j180.erp.validator.ResourceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源维护服务（菜单/页面/按钮/接口）
 */
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceMapper resourceMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final PermissionCache permissionCache;
    private final ResourceValidator resourceValidator;

    /**
     * 资源树查询（keyword / resType / status，父级未命中时自动保留）
     */
    public List<ResourceTreeNode> tree(ResourceQuery query) {
        if (query == null) {
            query = new ResourceQuery();
        }
        List<Resource> all = resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                .eq(query.getStatus() != null, Resource::getStatus, query.getStatus())
                .orderByAsc(Resource::getSortNo).orderByAsc(Resource::getId));
        Map<Long, Resource> map = all.stream().collect(Collectors.toMap(Resource::getId, r -> r));
        List<Resource> filtered = filter(all, map, query);
        return buildTree(filtered);
    }

    public Resource getById(Long id) {
        return mustExist(id);
    }

    public void create(ResourceForm form) {
        resourceValidator.validate(form);
        AssertUtil.isNull(selectByCode(form.getResCode()), "资源编码已存在");
        // 顶层仅允许菜单类型
        if (form.getParentId() == 0) {
            AssertUtil.isTrue(form.getResType() == ResourceTypeEnum.MENU.getCode(), "顶层资源仅允许菜单类型");
        } else {
            Resource parent = mustExist(form.getParentId());
            ResourceTypeEnum.checkHierarchy(parent.getResType(), form.getResType());
        }
        Resource resource = new Resource();
        BeanUtils.copyProperties(form, resource);
        if (resource.getSortNo() == null) {
            resource.setSortNo(0);
        }
        if (resource.getStatus() == null) {
            resource.setStatus(Constants.STATUS_ENABLED);
        }
        if (resource.getResType() == ResourceTypeEnum.API.getCode()
                && StringUtils.hasText(resource.getHttpMethod())) {
            resource.setHttpMethod(resource.getHttpMethod().toUpperCase());
        }
        resourceMapper.insert(resource);
    }

    public void update(ResourceForm form) {
        AssertUtil.notNull(form.getId(), "资源ID不能为空");
        resourceValidator.validate(form);
        Resource resource = mustExist(form.getId());
        AssertUtil.isFalse(selectByCode(form.getResCode()) != null
                        && !selectByCode(form.getResCode()).getId().equals(resource.getId()),
                "资源编码已存在");
        // 防止成环：上级不能是自身或自身子树
        if (form.getParentId() != 0 && !Objects.equals(resource.getParentId(), form.getParentId())) {
            AssertUtil.isFalse(isUnder(resource.getId(), form.getParentId()), "上级资源不能是自己的下级");
            Resource parent = mustExist(form.getParentId());
            ResourceTypeEnum.checkHierarchy(parent.getResType(), form.getResType());
        } else if (form.getParentId() == 0) {
            AssertUtil.isTrue(form.getResType() == ResourceTypeEnum.MENU.getCode(), "顶层资源仅允许菜单类型");
        }
        BeanUtils.copyProperties(form, resource, "id", "createTime", "updateTime");
        if (resource.getResType() == ResourceTypeEnum.API.getCode()
                && StringUtils.hasText(resource.getHttpMethod())) {
            resource.setHttpMethod(resource.getHttpMethod().toUpperCase());
        }
        resourceMapper.updateById(resource);
        evictAffectedRoles(resource.getId());
    }

    public void updateStatus(Long id, StatusForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        StatusEnum.check(form.getStatus());
        Resource resource = mustExist(id);
        resource.setStatus(form.getStatus());
        resourceMapper.updateById(resource);
        evictAffectedRoles(id);
    }

    public void delete(Long id) {
        Resource resource = mustExist(id);
        long childCount = resourceMapper.selectCount(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getParentId, id));
        AssertUtil.isTrue(childCount == 0, "存在下级资源，不可删除");
        long roleCount = roleResourceMapper.countRolesByResourceId(id);
        AssertUtil.isTrue(roleCount == 0, "资源已被角色授权，不可删除，请先在角色授权中移除");
        resourceMapper.deleteById(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    /**
     * 角色资源变更后失效对应角色缓存
     */
    public void evictAffectedRoles(Long resourceId) {
        List<RoleResource> binds = roleResourceMapper.selectList(new LambdaQueryWrapper<RoleResource>()
                .eq(RoleResource::getResourceId, resourceId));
        for (RoleResource bind : binds) {
            permissionCache.evictRole(bind.getRoleId());
        }
    }

    private Resource mustExist(Long id) {
        Resource resource = resourceMapper.selectById(id);
        AssertUtil.notNull(resource, "资源不存在或已删除");
        return resource;
    }

    private Resource selectByCode(String code) {
        return resourceMapper.selectOne(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getResCode, code).last("LIMIT 1"));
    }

    private boolean isUnder(Long nodeId, Long candidateParentId) {
        if (Objects.equals(nodeId, candidateParentId)) {
            return true;
        }
        return collectSubtreeIds(nodeId).contains(candidateParentId);
    }

    private Set<Long> collectSubtreeIds(Long id) {
        List<Resource> all = resourceMapper.selectList(null);
        Set<Long> result = new HashSet<>();
        collectChildren(all, result, id);
        result.add(id);
        return result;
    }

    private void collectChildren(List<Resource> all, Set<Long> result, Long parentId) {
        for (Resource r : all) {
            if (parentId.equals(r.getParentId())) {
                result.add(r.getId());
                collectChildren(all, result, r.getId());
            }
        }
    }

    private List<Resource> filter(List<Resource> all, Map<Long, Resource> map, ResourceQuery query) {
        List<Resource> keep = new ArrayList<>(all);
        if (query.getResType() != null) {
            keep = keep.stream().filter(r -> r.getResType().equals(query.getResType())).collect(Collectors.toList());
        }
        if (!StringUtils.hasText(query.getKeyword())) {
            return keep;
        }
        String keyword = query.getKeyword().trim();
        Set<Long> keepIds = new HashSet<>();
        for (Resource r : keep) {
            if (r.getResName().contains(keyword) || r.getResCode().contains(keyword)
                    || (r.getPath() != null && r.getPath().contains(keyword))) {
                Long cursor = r.getId();
                while (cursor != null && cursor != 0) {
                    keepIds.add(cursor);
                    Resource parent = map.get(cursor);
                    cursor = parent == null || parent.getParentId() == 0 ? null : parent.getParentId();
                }
            }
        }
        return keep.stream().filter(r -> keepIds.contains(r.getId())).collect(Collectors.toList());
    }

    private List<ResourceTreeNode> buildTree(List<Resource> list) {
        Map<Long, ResourceTreeNode> nodeMap = list.stream()
                .collect(Collectors.toMap(Resource::getId, this::toTreeNode));
        List<ResourceTreeNode> roots = new ArrayList<>();
        for (ResourceTreeNode node : nodeMap.values()) {
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
