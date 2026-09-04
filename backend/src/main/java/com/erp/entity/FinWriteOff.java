package com.erp.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 核销明细（应收/应付通用视图）
 */
@Data
public class FinWriteOff {
    private Long detailId;
    private Long listId;
    private BigDecimal amount;
}
