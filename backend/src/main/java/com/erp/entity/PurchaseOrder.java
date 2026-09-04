package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity {
    private String orderNo;
    private Long supplierId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;
    private BigDecimal allAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String status;
    private String auditStatus;
    private String approvePerson;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    private String orderStates;
    private Long vehicleId;
    private Long warehouseId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;
    private String remark;
    // 关联显示
    private String supplierName;
    private String warehouseName;
    private List<PurchaseOrderItem> items = new ArrayList<>();
}
