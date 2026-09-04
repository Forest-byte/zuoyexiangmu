package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMeeting extends BaseEntity {
    private String name;
    private Integer capacity;
    private String status;
}
