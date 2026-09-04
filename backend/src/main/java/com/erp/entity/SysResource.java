package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysResource extends BaseEntity {
    private Long parentId;
    private String name;
    private String type;
    private String code;
    private String path;
    private String icon;
    private Integer sort;
    private List<SysResource> children = new ArrayList<>();
}
