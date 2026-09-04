package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrmArcDetail extends BaseEntity {
    private Long customerId;
    private String refType;
    private String refNo;
    private BigDecimal amount;
    private BigDecimal received;
    private BigDecimal balance;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
