package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseDemand extends BaseEntity {
    private String demandNo;
    private Long goodsId;
    private BigDecimal quantity;
    private String note;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate needDate;
    private String applicant;
    private String status;
    // 关联显示
    private String goodsName;
}
