package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrmCategory extends BaseEntity {
    private Long parentId;
    private String name;
    private String kind;
    private List<CrmCategory> children = new ArrayList<>();
}
