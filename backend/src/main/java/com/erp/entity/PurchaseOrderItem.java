package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderItem extends BaseEntity {
    private Long orderId;
    private Long goodsId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal receivedQty;
    private String remark;
    // 关联显示
    private String goodsName;
    private String goodsCode;
    private String unitName;
}
