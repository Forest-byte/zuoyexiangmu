package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门信息表 sys_department（树形自关联，ancestors 冗余祖先路径含自身）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class Department extends BaseEntity {

    private Long parentId;

    private String ancestors;

    private String deptName;

    private String deptCode;

    private Integer sort;

    private Integer status;
}
