package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsStock extends BaseEntity {
    private Long warehouseId;
    private Long goodsId;
    private BigDecimal quantity;
    private String unit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    // 关联显示
    private String goodsName;
    private String goodsCode;
    private String categoryName;
    private BigDecimal lowLimit;
    private String status;
    private String warehouseName;
}
