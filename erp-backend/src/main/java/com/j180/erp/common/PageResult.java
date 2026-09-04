package com.j180.erp.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private long pageNum;
    private long pageSize;
    private long total;
    private long pages;
    private List<T> list;

    public static <T> PageResult<T> from(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.pageNum = page.getCurrent();
        result.pageSize = page.getSize();
        result.total = page.getTotal();
        result.pages = page.getPages();
        result.list = page.getRecords();
        return result;
    }

    /**
     * 分页对象转换（实体 -> VO）
     */
    public static <S, T> PageResult<T> from(Page<S> page, Function<S, T> converter) {
        PageResult<T> result = new PageResult<>();
        result.pageNum = page.getCurrent();
        result.pageSize = page.getSize();
        result.total = page.getTotal();
        result.pages = page.getPages();
        result.list = page.getRecords().stream().map(converter).collect(Collectors.toList());
        return result;
    }
}
