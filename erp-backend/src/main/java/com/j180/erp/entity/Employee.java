package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 员工信息表 sys_employee
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee")
public class Employee extends BaseEntity {

    private String empNo;

    private String name;

    /** 0=男 1=女 */
    private Integer gender;

    private String idCard;

    private String mobile;

    private String email;

    private Long departmentId;

    private String position;

    /** 职级 */
    private String level;

    private LocalDate hireDate;

    private LocalDate leaveDate;

    /** 0=试用 1=在职 2=离职 */
    private Integer status;

    private Long userId;

    private String remark;
}
