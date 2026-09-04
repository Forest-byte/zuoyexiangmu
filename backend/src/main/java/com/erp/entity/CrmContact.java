package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrmContact extends BaseEntity {
    private String partnerType;
    private Long partnerId;
    private String name;
    private String phone;
    private String email;
    private Integer isDefault;
}
