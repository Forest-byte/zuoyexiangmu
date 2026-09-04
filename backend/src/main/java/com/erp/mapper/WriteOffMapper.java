package com.erp.mapper;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface WriteOffMapper {

    @Insert("INSERT INTO fin_receipt_rel(list_id,arc_detail_id,amount,create_time,create_by) VALUES(#{listId},#{detailId},#{amount},NOW(),'system')")
    int insertReceiptRel(@Param("listId") Long listId, @Param("detailId") Long detailId, @Param("amount") BigDecimal amount);

    @Insert("INSERT INTO fin_payable_rel(list_id,ap_detail_id,amount,create_time,create_by) VALUES(#{listId},#{detailId},#{amount},NOW(),'system')")
    int insertPayableRel(@Param("listId") Long listId, @Param("detailId") Long detailId, @Param("amount") BigDecimal amount);

    @Select("SELECT arc_detail_id AS detailId, amount FROM fin_receipt_rel WHERE list_id=#{listId} AND deleted=0")
    java.util.List<java.util.Map<String, Object>> selectReceiptRels(@Param("listId") Long listId);

    @Select("SELECT ap_detail_id AS detailId, amount FROM fin_payable_rel WHERE list_id=#{listId} AND deleted=0")
    java.util.List<java.util.Map<String, Object>> selectPayableRels(@Param("listId") Long listId);
}
