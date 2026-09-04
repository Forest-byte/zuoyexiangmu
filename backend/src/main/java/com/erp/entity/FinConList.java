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
public class FinConList extends BaseEntity {
    private String listNo;
    private String listType;
    private String ordersKey;
    private Long partnerId;
    private Long accountId;
    private BigDecimal allMoney;
    private String payType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receiptDate;
    private String states;
    private String payer;
    private BigDecimal orderAmount;
    private String isDingdao;
    private String remark;
    // 关联显示
    private String partnerName;
    private String accountName;
    private List<FinWriteOff> details = new ArrayList<>();
}
