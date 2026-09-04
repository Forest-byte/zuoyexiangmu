package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsCheckItem extends BaseEntity {
    private Long checkId;
    private Long goodsId;
    private BigDecimal bookQty;
    private BigDecimal realQty;
    private BigDecimal diffQty;
    // 关联显示
    private String goodsName;
    private String goodsCode;
    private String unitName;
}
