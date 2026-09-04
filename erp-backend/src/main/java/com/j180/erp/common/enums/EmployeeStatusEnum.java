package com.j180.erp.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工状态枚举
 */
@Getter
@AllArgsConstructor
public enum EmployeeStatusEnum {

    TRIAL(0, "试用"),
    ON_JOB(1, "在职"),
    LEAVED(2, "离职");

    private final int code;
    private final String label;
}
