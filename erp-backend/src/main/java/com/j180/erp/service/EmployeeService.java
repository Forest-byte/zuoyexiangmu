package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j180.erp.common.Constants;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.EmployeeForm;
import com.j180.erp.dto.EmployeeQuery;
import com.j180.erp.dto.EmployeeVO;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.entity.Department;
import com.j180.erp.entity.Employee;
import com.j180.erp.entity.SysUser;
import com.j180.erp.mapper.DepartmentMapper;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.mapper.SysUserMapper;
import com.j180.erp.validator.EmployeeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 员工信息服务
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final SysUserMapper sysUserMapper;
    private final EmployeeValidator employeeValidator;

    /**
     * 条件分页查询（数据权限由拦截器按表注入）
     */
    public PageResult<EmployeeVO> page(EmployeeQuery query) {
        AssertUtil.notNull(query, "查询条件不能为空");
        query.validatePaging();
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Employee::getEmpNo, keyword)
                    .or().like(Employee::getName, keyword)
                    .or().like(Employee::getMobile, keyword)
                    .or().like(Employee::getIdCard, keyword));
        }
        wrapper.eq(query.getStatus() != null, Employee::getStatus, query.getStatus());
        wrapper.eq(query.getDepartmentId() != null, Employee::getDepartmentId, query.getDepartmentId());
        wrapper.orderByDesc(Employee::getId);
        Page<Employee> page = employeeMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page, this::toVO);
    }

    public EmployeeVO getVO(Long id) {
        Employee employee = mustExist(id);
        return toVO(employee);
    }

    public Employee getById(Long id) {
        return mustExist(id);
    }

    public List<Employee> listWorking() {
        return employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, Constants.EMP_ON_JOB)
                .orderByAsc(Employee::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(EmployeeForm form) {
        employeeValidator.validate(form);
        AssertUtil.isNull(selectByEmpNo(form.getEmpNo()), "员工编号已存在");
        checkIdCardUnique(form.getIdCard(), null);
        checkDepartmentExists(form.getDepartmentId());
        checkUserBinding(form.getUserId(), null);
        Employee employee = new Employee();
        BeanUtils.copyProperties(form, employee);
        if (employee.getStatus() == null) {
            employee.setStatus(Constants.EMP_ON_JOB);
        }
        if (StringUtils.hasText(employee.getIdCard())) {
            employee.setIdCard(employee.getIdCard().toUpperCase());
        }
        employeeMapper.insert(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(EmployeeForm form) {
        AssertUtil.notNull(form.getId(), "员工ID不能为空");
        employeeValidator.validate(form);
        Employee employee = mustExist(form.getId());
        AssertUtil.isFalse(selectByEmpNo(form.getEmpNo()) != null
                        && !selectByEmpNo(form.getEmpNo()).getId().equals(employee.getId()),
                "员工编号已存在");
        checkIdCardUnique(form.getIdCard(), employee.getId());
        checkDepartmentExists(form.getDepartmentId());
        checkUserBinding(form.getUserId(), employee.getId());
        BeanUtils.copyProperties(form, employee, "id", "createTime", "updateTime");
        if (StringUtils.hasText(employee.getIdCard())) {
            employee.setIdCard(employee.getIdCard().toUpperCase());
        }
        employeeMapper.updateById(employee);
    }

    /**
     * 员工离职：员工置离职，关联账号自动停用（即时回收登录与权限）
     */
    @Transactional(rollbackFor = Exception.class)
    public void leave(Long id) {
        Employee employee = mustExist(id);
        employeeValidator.validateLeave(employee.getStatus());
        employee.setStatus(Constants.EMP_LEAVED);
        employee.setLeaveDate(LocalDate.now());
        employeeMapper.updateById(employee);
        if (employee.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(employee.getUserId());
            if (user != null && user.getStatus() == Constants.STATUS_ENABLED) {
                user.setStatus(Constants.STATUS_DISABLED);
                sysUserMapper.updateById(user);
            }
        }
    }

    public void delete(Long id) {
        Employee employee = mustExist(id);
        AssertUtil.isTrue(employee.getStatus() == Constants.EMP_LEAVED, "仅离职状态的员工可删除");
        AssertUtil.isNull(employee.getUserId(), "员工仍关联登录账号，请先删除账号或解除绑定");
        employeeMapper.deleteById(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    private Employee mustExist(Long id) {
        Employee employee = employeeMapper.selectById(id);
        AssertUtil.notNull(employee, "员工不存在或已删除");
        return employee;
    }

    private Employee selectByEmpNo(String empNo) {
        return employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getEmpNo, empNo).last("LIMIT 1"));
    }

    private void checkIdCardUnique(String idCard, Long excludeId) {
        if (!StringUtils.hasText(idCard)) {
            return;
        }
        Employee exists = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getIdCard, idCard).last("LIMIT 1"));
        AssertUtil.isFalse(exists != null && (excludeId == null || !exists.getId().equals(excludeId)),
                "身份证号已存在");
    }

    private void checkDepartmentExists(Long departmentId) {
        if (departmentId == null) {
            return;
        }
        Department dept = departmentMapper.selectById(departmentId);
        AssertUtil.notNull(dept, "所属部门不存在");
    }

    private void checkUserBinding(Long userId, Long excludeEmpId) {
        if (userId == null) {
            return;
        }
        SysUser user = sysUserMapper.selectById(userId);
        AssertUtil.notNull(user, "关联登录账号不存在");
        AssertUtil.isTrue(user.getEmployeeId() == null
                        || (excludeEmpId != null && user.getEmployeeId().equals(excludeEmpId)),
                "该账号已关联其他员工");
    }

    private List<EmployeeVO> toVOList(List<Employee> employees) {
        return employees.stream().map(this::toVO).collect(Collectors.toList());
    }

    private EmployeeVO toVO(Employee employee) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(employee, vo);
        if (employee.getDepartmentId() != null) {
            Department dept = departmentMapper.selectById(employee.getDepartmentId());
            vo.setDepartmentName(dept == null ? null : dept.getDeptName());
        }
        if (employee.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(employee.getUserId());
            vo.setUsername(user == null ? null : user.getUsername());
        }
        return vo;
    }
}
