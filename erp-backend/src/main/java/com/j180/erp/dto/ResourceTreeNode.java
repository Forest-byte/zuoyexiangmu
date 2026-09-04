package com.j180.erp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源树节点（前端授权树 / 菜单树复用）
 */
@Data
public class ResourceTreeNode {

    private Long id;

    private String resCode;

    private String resName;

    /** 1=菜单 2=页面 3=按钮 4=接口 */
    private Integer resType;

    private Long parentId;

    private String path;

    private String httpMethod;

    private Integer sortNo;

    private String icon;

    private Integer status;

    /** 是否已勾选（授权树回显用） */
    private Boolean checked;

    private List<ResourceTreeNode> children = new ArrayList<>();
}
