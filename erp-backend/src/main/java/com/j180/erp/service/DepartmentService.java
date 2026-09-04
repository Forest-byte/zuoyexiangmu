package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j180.erp.common.Constants;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.DepartmentForm;
import com.j180.erp.dto.DeptTreeNode;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Department;
import com.j180.erp.entity.Employee;
import com.j180.erp.mapper.DepartmentMapper;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.validator.DepartmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 部门信息服务（树形自关联）
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentValidator departmentValidator;

    /**
     * 部门树查询（keyword 命中节点保留其祖先链）
     */
    public List<DeptTreeNode> tree(String keyword, Integer status) {
        List<Department> all = departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(status != null, Department::getStatus, status)
                .orderByAsc(Department::getSort).orderByAsc(Department::getId));
        Map<Long, Department> map = all.stream().collect(Collectors.toMap(Department::getId, Function.identity()));
        List<Department> filtered = filterByKeyword(all, map, keyword);
        return buildTree(filtered);
    }

    public Department getById(Long id) {
        return mustExist(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(DepartmentForm form) {
        departmentValidator.validate(form);
        AssertUtil.isNull(selectByCode(form.getDeptCode()), "部门编码已存在");
        AssertUtil.isFalse(checkSameLevelName(form.getParentId(), form.getDeptName(), null),
                "同级下已存在同名部门");
        Department parent = form.getParentId() == 0 ? null : mustExist(form.getParentId());
        Department dept = new Department();
        BeanUtils.copyProperties(form, dept);
        if (dept.getSort() == null) {
            dept.setSort(0);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(Constants.STATUS_ENABLED);
        }
        dept.setAncestors("0");
        departmentMapper.insert(dept);
        String parentAncestors = parent == null ? "0" : parent.getAncestors();
        String ancestors = parentAncestors + "," + dept.getId();
        dept.setAncestors(ancestors);
        departmentMapper.updateById(dept);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(DepartmentForm form) {
        AssertUtil.notNull(form.getId(), "部门ID不能为空");
        departmentValidator.validate(form);
        Department dept = mustExist(form.getId());
        AssertUtil.isFalse(selectByCode(form.getDeptCode()) != null
                        && !selectByCode(form.getDeptCode()).getId().equals(dept.getId()),
                "部门编码已存在");
        AssertUtil.isFalse(checkSameLevelName(form.getParentId(), form.getDeptName(), dept.getId()),
                "同级下已存在同名部门");
        boolean parentChanged = !Objects.equals(dept.getParentId(), form.getParentId());
        if (parentChanged) {
            // 上级不能是自身或自身子树（防止成环）
            AssertUtil.isFalse(isUnder(dept.getId(), form.getParentId()), "上级部门不能是自己的子部门");
            Department parent = form.getParentId() == 0 ? null : mustExist(form.getParentId());
            String parentAncestors = parent == null ? "0" : parent.getAncestors();
            BeanUtils.copyProperties(form, dept, "id", "ancestors", "createTime", "updateTime");
            dept.setAncestors(parentAncestors + "," + dept.getId());
            departmentMapper.updateById(dept);
            rebuildSubtreeAncestors(dept.getId(), dept.getAncestors());
        } else {
            BeanUtils.copyProperties(form, dept, "id", "ancestors", "createTime", "updateTime");
            departmentMapper.updateById(dept);
        }
    }

    public void updateStatus(Long id, StatusForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        StatusEnum.check(form.getStatus());
        Department dept = mustExist(id);
        if (form.getStatus() == Constants.STATUS_DISABLED && dept.getStatus() == Constants.STATUS_ENABLED) {
            long enabledChild = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                    .eq(Department::getParentId, id).eq(Department::getStatus, Constants.STATUS_ENABLED));
            AssertUtil.isTrue(enabledChild == 0, "存在启用中的子部门，请先停用子部门");
        }
        dept.setStatus(form.getStatus());
        departmentMapper.updateById(dept);
    }

    public void delete(Long id) {
        Department dept = mustExist(id);
        long childCount = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, id));
        AssertUtil.isTrue(childCount == 0, "存在下级部门，不可删除");
        long empCount = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getDepartmentId, id));
        AssertUtil.isTrue(empCount == 0, "部门下存在员工，不可删除");
        departmentMapper.deleteById(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    public List<Long> collectSubtreeIds(Long id) {
        List<Department> all = departmentMapper.selectList(null);
        List<Long> result = new ArrayList<>();
        collectChildren(all, result, id);
        result.add(id);
        return result;
    }

    private void collectChildren(List<Department> all, List<Long> result, Long parentId) {
        for (Department d : all) {
            if (parentId.equals(d.getParentId())) {
                result.add(d.getId());
                collectChildren(all, result, d.getId());
            }
        }
    }

    /**
     * 重建某节点下所有后代的 ancestors 路径
     */
    private void rebuildSubtreeAncestors(Long rootId, String rootAncestors) {
        List<Department> all = departmentMapper.selectList(null);
        updateChildrenAncestors(all, rootAncestors, rootId);
    }

    private void updateChildrenAncestors(List<Department> all, String parentAncestors, Long parentId) {
        for (Department d : all) {
            if (parentId.equals(d.getParentId())) {
                String newAncestors = parentAncestors + "," + d.getId();
                if (!newAncestors.equals(d.getAncestors())) {
                    d.setAncestors(newAncestors);
                    departmentMapper.updateById(d);
                }
                updateChildrenAncestors(all, newAncestors, d.getId());
            }
        }
    }

    private Department mustExist(Long id) {
        Department dept = departmentMapper.selectById(id);
        AssertUtil.notNull(dept, "部门不存在或已删除");
        return dept;
    }

    private Department selectByCode(String code) {
        return departmentMapper.selectOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getDeptCode, code).last("LIMIT 1"));
    }

    /**
     * 同级重名校验
     */
    private boolean checkSameLevelName(Long parentId, String deptName, Long excludeId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, parentId)
                .eq(Department::getDeptName, deptName);
        if (excludeId != null) {
            wrapper.ne(Department::getId, excludeId);
        }
        return departmentMapper.selectCount(wrapper) > 0;
    }

    /**
     * candidateParentId 是否位于 nodeId 的子树中（用于防环）
     */
    private boolean isUnder(Long nodeId, Long candidateParentId) {
        if (Objects.equals(nodeId, candidateParentId)) {
            return true;
        }
        return collectSubtreeIds(nodeId).contains(candidateParentId);
    }

    private List<Department> filterByKeyword(List<Department> all, Map<Long, Department> map, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return all;
        }
        String kw = keyword.trim();
        Set<Long> keepIds = new HashSet<>();
        for (Department d : all) {
            if (d.getDeptName().contains(kw) || d.getDeptCode().contains(kw)) {
                Long cursor = d.getId();
                while (cursor != null && cursor != 0) {
                    keepIds.add(cursor);
                    Department parent = map.get(cursor);
                    cursor = parent == null || parent.getParentId() == 0 ? null : parent.getParentId();
                }
            }
        }
        return all.stream().filter(d -> keepIds.contains(d.getId())).collect(Collectors.toList());
    }

    private List<DeptTreeNode> buildTree(List<Department> list) {
        Map<Long, DeptTreeNode> nodeMap = list.stream()
                .collect(Collectors.toMap(Department::getId, this::toTreeNode));
        List<DeptTreeNode> roots = new ArrayList<>();
        for (DeptTreeNode node : nodeMap.values()) {
            DeptTreeNode parent = node.getParentId() == null ? null : nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private DeptTreeNode toTreeNode(Department d) {
        DeptTreeNode node = new DeptTreeNode();
        node.setId(d.getId());
        node.setParentId(d.getParentId());
        node.setAncestors(d.getAncestors());
        node.setDeptName(d.getDeptName());
        node.setDeptCode(d.getDeptCode());
        node.setSort(d.getSort());
        node.setStatus(d.getStatus());
        node.setChildren(new ArrayList<>());
        return node;
    }
}
