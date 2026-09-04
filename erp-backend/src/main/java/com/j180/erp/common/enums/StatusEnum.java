package com.j180.erp.common.enums;

import com.j180.erp.common.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举（公司/部门/角色/资源/仓库/账号 启用停用）
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    private final int code;
    private final String label;

    public static void check(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态值非法，仅允许 0=停用 1=启用");
        }
    }
}
