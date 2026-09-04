package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysParam extends BaseEntity {
    private String paramKey;
    private String paramValue;
    private String description;
}
