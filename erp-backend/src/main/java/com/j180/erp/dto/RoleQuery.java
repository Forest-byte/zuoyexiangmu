package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {

    private String keyword;

    private Integer status;
}
