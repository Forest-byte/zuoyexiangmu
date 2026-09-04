package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WarehouseQuery extends PageQuery {

    private String keyword;

    private Integer whType;

    private Integer status;
}
