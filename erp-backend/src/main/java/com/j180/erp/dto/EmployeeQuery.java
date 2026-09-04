package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeQuery extends PageQuery {

    /** 关键字：编号/姓名/手机号模糊 */
    private String keyword;

    /** 0=试用 1=在职 2=离职 */
    private Integer status;

    private Long departmentId;
}
