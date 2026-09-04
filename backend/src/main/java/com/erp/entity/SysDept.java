package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDept extends BaseEntity {
    private String name;
    private String code;
    private Long companyId;
    private String manager;
    private String phone;
}
