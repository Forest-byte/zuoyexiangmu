package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentQuery extends PageQuery {

    private String keyword;

    private Integer status;
}
