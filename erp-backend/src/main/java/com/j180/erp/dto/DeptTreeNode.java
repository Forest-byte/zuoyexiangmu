package com.j180.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 部门树节点
 */
@Data
public class DeptTreeNode {

    private Long id;

    private Long parentId;

    private String ancestors;

    private String deptName;

    private String deptCode;

    private Integer sort;

    private Integer status;

    private List<DeptTreeNode> children;
}
