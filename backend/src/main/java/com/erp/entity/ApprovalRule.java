package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalRule extends BaseEntity {
    private String docType;
    private String roleCode;
    private Integer level;
    private Integer enabled;
}
