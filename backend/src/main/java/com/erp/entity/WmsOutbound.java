package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsOutbound extends BaseEntity {
    private String outNo;
    private String outType;
    private String srcNo;
    private Long warehouseId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate outDate;
    private String operator;
    private String status;
    // 关联显示
    private String warehouseName;
    private List<WmsOutboundItem> items = new ArrayList<>();
}
