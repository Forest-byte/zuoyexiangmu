package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysVehicle extends BaseEntity {
    private String name;
    private String code;
    private String type;
    private String capacity;
    private String driver;
    private String phone;
    private String status;
}
