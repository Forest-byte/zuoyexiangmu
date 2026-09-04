package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Goods extends BaseEntity {
    private String code;
    private String name;
    private Long categoryId;
    private Long unitId;
    private String spec;
    private String brand;
    private String barcode;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal lastInPrice;
    private BigDecimal lowLimit;
    private BigDecimal highLimit;
    private Long supplierId;
    private Integer isRaw;
    private String status;
    // 关联显示
    private String categoryName;
    private String unitName;
    private BigDecimal stockQty;
    private Long warehouseId;
}
