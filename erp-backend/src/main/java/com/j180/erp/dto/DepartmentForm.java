package com.j180.erp.dto;

import lombok.Data;

/**
 * 部门新增/编辑表单
 */
@Data
public class DepartmentForm {

    private Long id;

    /** 上级部门ID，0=顶级 */
    private Long parentId;

    private String deptName;

    private String deptCode;

    private Integer sort;

    private Integer status;
}
