package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinAccount extends BaseEntity {
    private String name;
    private String accountNo;
    private BigDecimal beginBalance;
    private BigDecimal balance;
    private String bank;
    private String status;
}
