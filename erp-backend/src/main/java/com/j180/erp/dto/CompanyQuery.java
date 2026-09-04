package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公司信息查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyQuery extends PageQuery {

    /** 关键字：编码/名称/信用代码模糊 */
    private String keyword;

    /** 状态：0=停用 1=启用 */
    private Integer status;
}
