package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录账号查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    /** 按登录名/员工姓名模糊匹配 */
    private String keyword;

    private Integer status;

    /** 按角色筛选 */
    private Long roleId;
}
