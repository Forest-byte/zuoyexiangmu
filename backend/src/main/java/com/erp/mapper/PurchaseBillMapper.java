package com.erp.mapper;

import com.erp.entity.PurchaseBill;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PurchaseBillMapper {

    @Select("SELECT b.*, o.order_no AS orderNo, s.name AS supplierName FROM purchase_bill b " +
            "LEFT JOIN purchase_order o ON b.order_id=o.id LEFT JOIN crm_supplier s ON o.supplier_id=s.id " +
            "WHERE b.deleted=0 ORDER BY b.id DESC")
    List<PurchaseBill> selectAll();

    @Select("SELECT * FROM purchase_bill WHERE order_id=#{orderId} AND deleted=0")
    List<PurchaseBill> selectByOrder(@Param("orderId") Long orderId);

    @Insert("INSERT INTO purchase_bill(order_id,bill_type,bill_no,amount,file_url,register_time,create_time,create_by) " +
            "VALUES(#{orderId},#{billType},#{billNo},#{amount},#{fileUrl},NOW(),NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseBill b);

    @Update("UPDATE purchase_bill SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
