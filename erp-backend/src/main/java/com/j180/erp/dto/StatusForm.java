package com.j180.erp.dto;

import lombok.Data;

/**
 * 启用/停用表单（统一接口）
 */
@Data
public class StatusForm {

    /** 0=停用 1=启用 */
    private Integer status;
}
