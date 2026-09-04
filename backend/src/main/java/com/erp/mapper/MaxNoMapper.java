package com.erp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通用单号最大值查询（动态表/列）
 */
@Mapper
public interface MaxNoMapper {

    @Select("SELECT MAX(${column}) FROM ${table} WHERE ${column} LIKE CONCAT(#{prefix}, #{dateStr}, '%')")
    String maxBillNo(@Param("table") String table,
                     @Param("column") String column,
                     @Param("prefix") String prefix,
                     @Param("dateStr") String dateStr);
}
