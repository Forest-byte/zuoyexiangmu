package com.j180.erp.dto;

import lombok.Data;

/**
 * 角色表单
 */
@Data
public class RoleForm {

    private Long id;

    /** 角色编码（全局唯一） */
    private String roleCode;

    /** 角色名称（全局唯一） */
    private String roleName;

    private String description;

    /** 1=全部 2=本部门及子部门 3=本部门 4=本人 5=本仓库 */
    private Integer dataScope;

    /** 数据范围明细ID（逗号分隔，可选） */
    private String dataScopeIds;

    private Integer status;

    private String remark;
}
