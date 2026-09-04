package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinAccountLog extends BaseEntity {
    private Long accountId;
    private String bizType;
    private String refNo;
    private BigDecimal inAmount;
    private BigDecimal outAmount;
    private BigDecimal balanceAfter;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;
    private String operator;
    // 关联显示
    private String accountName;
}
