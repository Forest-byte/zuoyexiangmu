package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表 sys_role
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    private String roleCode;

    private String roleName;

    private String description;

    /** 1=全部 2=本部门及子部门 3=本部门 4=本人 5=本仓库 */
    private Integer dataScope;

    /** 数据范围明细ID（逗号分隔，可选） */
    private String dataScopeIds;

    /** 1=内置角色 */
    private Integer isBuiltin;

    private Integer status;

    private String remark;
}
