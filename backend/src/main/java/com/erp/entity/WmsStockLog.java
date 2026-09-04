package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsStockLog extends BaseEntity {
    private Long goodsId;
    private Long warehouseId;
    private String changeType;
    private BigDecimal changeQty;
    private BigDecimal beforeQty;
    private BigDecimal afterQty;
    private String refNo;
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeTime;
    // 关联显示
    private String goodsName;
    private String warehouseName;
}
