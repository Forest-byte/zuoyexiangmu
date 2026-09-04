package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysRegion extends BaseEntity {
    private String name;
    private Long parentId;
    private Integer sort;
    private List<SysRegion> children = new ArrayList<>();
}
