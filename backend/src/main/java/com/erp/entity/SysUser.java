package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String name;
    private String roleCode;
    private Long deptId;
    private Long employeeId;
    private Integer status;
}
