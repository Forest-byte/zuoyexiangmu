package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysCodeRule extends BaseEntity {
    private String docType;
    private String prefix;
    private String format;
    private Integer seqLen;
}
