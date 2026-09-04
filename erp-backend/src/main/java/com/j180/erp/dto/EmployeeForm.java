package com.j180.erp.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工新增/编辑表单
 */
@Data
public class EmployeeForm {

    private Long id;

    private String empNo;

    private String name;

    private Integer gender;

    private String idCard;

    private String mobile;

    private String email;

    private Long departmentId;

    private String position;

    private String level;

    private LocalDate hireDate;

    /** 0=试用 1=在职（离职须走离职处理） */
    private Integer status;

    /** 关联登录账号（可空=解绑） */
    private Long userId;

    private String remark;
}
