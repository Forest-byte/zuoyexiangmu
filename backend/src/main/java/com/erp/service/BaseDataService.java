package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.*;
import com.erp.mapper.*;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 基础维护服务：地区/分公司/部门/员工/仓库
 */
@Service
public class BaseDataService {

    private final RegionMapper regionMapper;
    private final CompanyMapper companyMapper;
    private final DeptMapper deptMapper;
    private final EmployeeMapper employeeMapper;
    private final WarehouseMapper warehouseMapper;
    private final AuditService auditService;

    public BaseDataService(RegionMapper regionMapper, CompanyMapper companyMapper, DeptMapper deptMapper,
                           EmployeeMapper employeeMapper, WarehouseMapper warehouseMapper, AuditService auditService) {
        this.regionMapper = regionMapper;
        this.companyMapper = companyMapper;
        this.deptMapper = deptMapper;
        this.employeeMapper = employeeMapper;
        this.warehouseMapper = warehouseMapper;
        this.auditService = auditService;
    }

    // ==================== 地区 ====================
    public List<SysRegion> regionTree() {
        List<SysRegion> all = regionMapper.selectAll();
        Map<Long, List<SysRegion>> byParent = new java.util.LinkedHashMap<>();
        for (SysRegion r : all) byParent.computeIfAbsent(r.getParentId() == null ? 0L : r.getParentId(), k -> new ArrayList<>()).add(r);
        List<SysRegion> roots = new ArrayList<>();
        for (SysRegion r : byParent.getOrDefault(0L, new ArrayList<>())) {
            fillRegion(r, byParent);
            roots.add(r);
        }
        return roots;
    }

    private void fillRegion(SysRegion p, Map<Long, List<SysRegion>> byParent) {
        List<SysRegion> children = byParent.getOrDefault(p.getId(), new ArrayList<>());
        for (SysRegion c : children) fillRegion(c, byParent);
        p.setChildren(children);
    }

    @Transactional
    public void saveRegion(SysRegion r) {
        if (r.getId() == null) {
            r.setCreateBy(UserContext.currentName());
            regionMapper.insert(r);
            auditService.log("新增地区", r.getName(), "", "");
        } else {
            regionMapper.update(r);
            auditService.log("编辑地区", r.getName(), "", "");
        }
    }

    @Transactional
    public void deleteRegion(Long id) {
        if (regionMapper.countChildren(id) > 0) throw new BusinessException("存在子地区，禁止删除");
        if (regionMapper.countCompanyRef(id) > 0) throw new BusinessException("地区已被公司引用，请先解除关联");
        regionMapper.delete(id);
        auditService.log("删除地区", String.valueOf(id), "", "");
    }

    // ==================== 分公司 ====================
    public List<SysCompany> companies() { return companyMapper.selectAll(); }

    @Transactional
    public void saveCompany(SysCompany c) {
        if (c.getId() == null) {
            c.setCreateBy(UserContext.currentName());
            companyMapper.insert(c);
            auditService.log("新增分公司", c.getName(), "", "");
        } else {
            companyMapper.update(c);
            auditService.log("编辑分公司", c.getName(), "", "");
        }
    }

    @Transactional
    public void deleteCompany(Long id) {
        if (companyMapper.countDeptRef(id) > 0) throw new BusinessException("公司下有部门，禁止删除，可改为停用");
        companyMapper.delete(id);
        auditService.log("删除分公司", String.valueOf(id), "", "");
    }

    // ==================== 部门 ====================
    public List<SysDept> depts(Long companyId) {
        return companyId == null ? deptMapper.selectAll() : deptMapper.selectByCompany(companyId);
    }

    @Transactional
    public void saveDept(SysDept d) {
        if (d.getId() == null) {
            d.setCreateBy(UserContext.currentName());
            deptMapper.insert(d);
            auditService.log("新增部门", d.getName(), "", "");
        } else {
            deptMapper.update(d);
            auditService.log("编辑部门", d.getName(), "", "");
        }
    }

    @Transactional
    public void deleteDept(Long id) {
        if (deptMapper.countEmployeeRef(id) > 0) throw new BusinessException("部门下有员工，禁止删除");
        deptMapper.delete(id);
        auditService.log("删除部门", String.valueOf(id), "", "");
    }

    // ==================== 员工 ====================
    public PageResult<SysEmployee> employeePage(String keyword, Long deptId, int page, int pageSize) {
        long total = employeeMapper.count(keyword, deptId);
        List<SysEmployee> list = employeeMapper.page(keyword, deptId, (page - 1) * pageSize, pageSize);
        return PageResult.of(total, list);
    }

    /** 保存员工：同步开账号（初始密码 123456），分配角色 */
    @Transactional
    public void saveEmployee(SysEmployee e) {
        if (e.getId() == null) {
            e.setCreateBy(UserContext.currentName());
            employeeMapper.insert(e);
            // 创建账号
            SysUser u = new SysUser();
            u.setUsername(e.getCode().toLowerCase());
            u.setPassword("123456");
            u.setName(e.getName());
            u.setRoleCode("ROLE_PURCHASE");
            u.setEmployeeId(e.getId());
            u.setDeptId(e.getDeptId());
            u.setStatus(1);
            u.setCreateBy(UserContext.currentName());
            // 使用 EmployeeMapper 内联插入用户？需要 UserMapper。此处直接使用 UserMapper
            // 通过构造器注入 UserMapper 会更清晰，这里使用 Spring 上下文不可行，简化：员工保存账号通过单独方法
            createUserForEmployee(u);
            assignRoles(e.getId(), e.getRoleIds());
            auditService.log("新增员工", e.getName(), "", "");
        } else {
            employeeMapper.update(e);
            // 同步姓名
            Long uid = employeeMapper.findUserIdByEmployee(e.getId());
            if (uid != null) {
                SysUser u = new SysUser();
                u.setId(uid);
                u.setUsername(e.getCode().toLowerCase());
                u.setName(e.getName());
                u.setDeptId(e.getDeptId());
                employeeMapper.syncUserForUpdate(u);
            }
            employeeMapper.deleteUserRolesByEmployee(e.getId());
            assignRoles(e.getId(), e.getRoleIds());
            auditService.log("编辑员工", e.getName(), "", "");
        }
    }

    private void createUserForEmployee(SysUser u) {
        employeeMapper.insertUserAccount(u);
    }

    private void assignRoles(Long employeeId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        Long uid = employeeMapper.findUserIdByEmployee(employeeId);
        if (uid == null) return;
        for (Long rid : roleIds) {
            employeeMapper.insertUserRole(uid, rid);
        }
    }

    @Transactional
    public void deleteEmployee(Long id) {
        employeeMapper.delete(id);
        auditService.log("删除员工", String.valueOf(id), "", "");
    }

    // ==================== 仓库 ====================
    public List<SysWarehouse> warehouses(String keyword) { return warehouseMapper.selectList(keyword); }

    @Transactional
    public void saveWarehouse(SysWarehouse w) {
        if (w.getId() == null) {
            w.setCreateBy(UserContext.currentName());
            warehouseMapper.insert(w);
            auditService.log("新增仓库", w.getName(), "", "");
        } else {
            warehouseMapper.update(w);
            auditService.log("编辑仓库", w.getName(), "", "");
        }
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        warehouseMapper.delete(id);
        auditService.log("删除仓库", String.valueOf(id), "", "");
    }
}
