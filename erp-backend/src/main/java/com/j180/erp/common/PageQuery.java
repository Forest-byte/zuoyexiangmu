package com.j180.erp.common;

import com.j180.erp.common.util.AssertUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基类：pageNum 从 1 开始，pageSize 不能大于 50
 */
@Data
public class PageQuery implements Serializable {

    public static final int MAX_PAGE_SIZE = 50;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /**
     * 校验并规范化分页参数
     */
    public void validatePaging() {
        if (pageNum == null || pageNum < 1) {
            this.pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            this.pageSize = 10;
        }
        AssertUtil.isTrue(pageSize <= MAX_PAGE_SIZE, "每页条数不能大于" + MAX_PAGE_SIZE);
    }
}
