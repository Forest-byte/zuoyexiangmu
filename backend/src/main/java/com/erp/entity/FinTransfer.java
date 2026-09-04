package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinTransfer extends BaseEntity {
    private String transferNo;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private String status;
    private String applicant;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;
    // 关联显示
    private String fromName;
    private String toName;
}
