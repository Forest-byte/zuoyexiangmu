package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.Result;
import com.j180.erp.dto.EmployeeForm;
import com.j180.erp.dto.EmployeeQuery;
import com.j180.erp.dto.EmployeeVO;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.entity.Employee;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工信息接口
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/page")
    @RequiresPermission("P_EMPLOYEE")
    public Result<PageResult<EmployeeVO>> page(EmployeeQuery query) {
        return Result.ok(employeeService.page(query));
    }

    @GetMapping("/list-working")
    @RequiresPermission("P_EMPLOYEE")
    public Result<List<Employee>> listWorking() {
        return Result.ok(employeeService.listWorking());
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_EMPLOYEE")
    public Result<EmployeeVO> detail(@PathVariable Long id) {
        return Result.ok(employeeService.getVO(id));
    }

    @PostMapping
    @RequiresPermission("B_EMP_ADD")
    @Audit(module = "员工管理", action = "新增员工", targetType = "sys_employee")
    public Result<Void> create(@RequestBody EmployeeForm form) {
        employeeService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_EMP_EDIT")
    @Audit(module = "员工管理", action = "编辑员工", targetType = "sys_employee")
    public Result<Void> update(@RequestBody EmployeeForm form) {
        employeeService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/leave")
    @RequiresPermission("B_EMP_LEAVE")
    @Audit(module = "员工管理", action = "员工离职", targetType = "sys_employee")
    public Result<Void> leave(@PathVariable Long id) {
        employeeService.leave(id);
        return Result.ok();
    }

    /**
     * 批量导入员工：入参为员工表单数组，逐条按新增规则校验，返回导入统计
     */
    @PostMapping("/import")
    @RequiresPermission("B_EMP_IMPORT")
    @Audit(module = "员工管理", action = "批量导入员工", targetType = "sys_employee")
    public Result<Map<String, Object>> importBatch(@RequestBody List<EmployeeForm> forms) {
        int total = forms == null ? 0 : forms.size();
        int success = 0;
        List<String> errors = new ArrayList<>();
        if (forms != null) {
            for (int i = 0; i < forms.size(); i++) {
                try {
                    employeeService.create(forms.get(i));
                    success++;
                } catch (RuntimeException e) {
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("fail", total - success);
        result.put("errors", errors);
        return Result.ok(result);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_EMP_DELETE")
    @Audit(module = "员工管理", action = "删除员工", targetType = "sys_employee")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_EMP_DELETE")
    @Audit(module = "员工管理", action = "批量删除员工", targetType = "sys_employee")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        employeeService.deleteBatch(form);
        return Result.ok();
    }
}
