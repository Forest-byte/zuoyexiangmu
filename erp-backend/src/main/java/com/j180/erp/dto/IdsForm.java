package com.j180.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量删除请求体 { ids: [1,2,3] }
 */
@Data
public class IdsForm {

    private List<Long> ids;
}
