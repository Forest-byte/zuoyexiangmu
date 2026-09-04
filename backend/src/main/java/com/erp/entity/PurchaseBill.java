package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseBill extends BaseEntity {
    private Long orderId;
    private String billType;
    private String billNo;
    private BigDecimal amount;
    private String fileUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;
    // 关联显示
    private String orderNo;
    private String supplierName;
}
