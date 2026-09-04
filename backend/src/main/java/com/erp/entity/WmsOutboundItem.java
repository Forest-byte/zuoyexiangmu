package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsOutboundItem extends BaseEntity {
    private Long outboundId;
    private Long goodsId;
    private BigDecimal quantity;
    // 关联显示
    private String goodsName;
    private String goodsCode;
    private String unitName;
}
