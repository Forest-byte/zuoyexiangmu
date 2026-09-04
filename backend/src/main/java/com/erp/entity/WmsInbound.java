package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInbound extends BaseEntity {
    private String inNo;
    private String inType;
    private String srcNo;
    private Long warehouseId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inDate;
    private BigDecimal totalAmount;
    private String operator;
    private String status;
    // 关联显示
    private String warehouseName;
    private List<WmsInboundItem> items = new ArrayList<>();
}
