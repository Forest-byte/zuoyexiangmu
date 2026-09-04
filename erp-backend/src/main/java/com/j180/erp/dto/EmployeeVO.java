package com.j180.erp.dto;

import com.j180.erp.entity.Employee;
import com.j180.erp.common.util.DesensitizeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工展示VO：补充部门名称/关联账号名，敏感字段脱敏
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeVO extends Employee {

    private String departmentName;

    private String username;

    /** 脱敏身份证号 */
    private String idCardMasked;

    /** 脱敏手机号 */
    private String mobileMasked;

    public static EmployeeVO from(Employee employee) {
        EmployeeVO vo = new EmployeeVO();
        vo.setId(employee.getId());
        vo.setEmpNo(employee.getEmpNo());
        vo.setName(employee.getName());
        vo.setGender(employee.getGender());
        vo.setIdCard(employee.getIdCard());
        vo.setMobile(employee.getMobile());
        vo.setEmail(employee.getEmail());
        vo.setDepartmentId(employee.getDepartmentId());
        vo.setPosition(employee.getPosition());
        vo.setLevel(employee.getLevel());
        vo.setHireDate(employee.getHireDate());
        vo.setLeaveDate(employee.getLeaveDate());
        vo.setStatus(employee.getStatus());
        vo.setUserId(employee.getUserId());
        vo.setRemark(employee.getRemark());
        vo.setCreateTime(employee.getCreateTime());
        vo.setUpdateTime(employee.getUpdateTime());
        vo.setIdCardMasked(DesensitizeUtil.idCard(employee.getIdCard()));
        vo.setMobileMasked(DesensitizeUtil.mobile(employee.getMobile()));
        return vo;
    }
}
