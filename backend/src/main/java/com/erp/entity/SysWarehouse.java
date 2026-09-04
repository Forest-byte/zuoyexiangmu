package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysWarehouse extends BaseEntity {
    private String name;
    private String code;
    private String address;
    private String manager;
    private String phone;
    private String status;
}
