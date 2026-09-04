package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysCompany extends BaseEntity {
    private String name;
    private String code;
    private Long regionId;
    private String address;
    private String phone;
    private String status;
}
