package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInboundItem extends BaseEntity {
    private Long inboundId;
    private Long goodsId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    // 关联显示
    private String goodsName;
    private String goodsCode;
    private String unitName;
}
