package com.j180.erp.common.enums;

import com.j180.erp.common.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围枚举：rank 越大范围越宽（多角色取最宽）
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL(1, 5, "全部数据"),
    DEPT_AND_CHILD(2, 4, "本部门及子部门"),
    DEPT(3, 3, "本部门"),
    SELF(4, 1, "本人"),
    WAREHOUSE(5, 2, "本仓库");

    private final int code;
    private final int rank;
    private final String label;

    public static DataScopeEnum of(Integer code) {
        for (DataScopeEnum scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        throw new BizException("非法的数据权限范围: " + code);
    }
}
