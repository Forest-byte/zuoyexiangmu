package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysEmployee extends BaseEntity {
    private String name;
    private String code;
    private Long deptId;
    private String position;
    private String phone;
    private String email;
    private String status;
    // 角色分配（非表字段）
    private java.util.List<Long> roleIds;
}
