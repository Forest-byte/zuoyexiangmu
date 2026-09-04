package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrmApDetail extends BaseEntity {
    private Long supplierId;
    private String refType;
    private String refNo;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal balance;
    private String status;
    private java.time.LocalDate dueDate;
}
