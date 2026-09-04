package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDict extends BaseEntity {
    private String dictType;
    private String label;
    private String value;
    private Integer sort;
}
