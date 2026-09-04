package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsCheck extends BaseEntity {
    private String checkNo;
    private Long warehouseId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkDate;
    private String status;
    private String checker;
    // 关联显示
    private String warehouseName;
    private List<WmsCheckItem> items = new ArrayList<>();
}
