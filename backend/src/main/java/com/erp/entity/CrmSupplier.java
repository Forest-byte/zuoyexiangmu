package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrmSupplier extends BaseEntity {
    private String code;
    private String name;
    private Long categoryId;
    private String linkman;
    private String phone;
    private String address;
    private BigDecimal payableAmount;
    private String status;
}
