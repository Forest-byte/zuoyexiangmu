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
public class SaleOrder extends BaseEntity {
    private String orderNo;
    private Long customerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    private BigDecimal allAmount;
    private BigDecimal discount;
    private BigDecimal receivedAmount;
    private String status;
    private String auditStatus;
    private String orderStates;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private Long warehouseId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;
    private String settleStatus;
    private String settlePerson;
    private String remark;
    // 关联显示
    private String customerName;
    private String warehouseName;
    private List<SaleOrderItem> items = new ArrayList<>();
}
