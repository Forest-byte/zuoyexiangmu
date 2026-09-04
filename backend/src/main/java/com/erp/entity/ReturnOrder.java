package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReturnOrder extends BaseEntity {
    private String returnNo;
    private String srcType;
    private Long srcId;
    private Long partnerId;
    private String reason;
    private BigDecimal amount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate;
    // 关联显示
    private String srcNo;
    private String partnerName;
}
