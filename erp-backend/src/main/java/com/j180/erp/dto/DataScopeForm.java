package com.j180.erp.dto;

import lombok.Data;

/**
 * 角色数据权限设置表单
 */
@Data
public class DataScopeForm {

    private Long roleId;

    /** 1=全部 2=本部门及子部门 3=本部门 4=本人 5=本仓库 */
    private Integer dataScope;

    /** 部门/仓库明细ID（逗号分隔，可选） */
    private String dataScopeIds;
}
