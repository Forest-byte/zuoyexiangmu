package com.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrmFollowRecord extends BaseEntity {
    private Long customerId;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextTime;
    private String recorder;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
}
