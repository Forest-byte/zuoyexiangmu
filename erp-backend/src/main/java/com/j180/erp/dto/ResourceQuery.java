package com.j180.erp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源查询条件
 */
@Data
public class ResourceQuery {

    private String keyword;

    private Integer resType;

    private Integer status;
}
