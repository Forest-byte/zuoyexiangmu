package com.erp.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsCategory extends BaseEntity {
    private Long parentId;
    private String name;
    private List<GoodsCategory> children = new ArrayList<>();
}
