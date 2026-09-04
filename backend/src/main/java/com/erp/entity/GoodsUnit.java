package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsUnit extends BaseEntity {
    private String name;
    private BigDecimal rate;
}
