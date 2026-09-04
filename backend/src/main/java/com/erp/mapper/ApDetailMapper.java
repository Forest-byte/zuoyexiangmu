package com.erp.mapper;

import com.erp.entity.CrmApDetail;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ApDetailMapper {

    @Select("<script>SELECT a.*, s.name AS supplierName FROM crm_ap_detail a LEFT JOIN crm_supplier s ON a.supplier_id=s.id " +
            "WHERE a.deleted=0 " +
            "<if test='supplierId!=null'> AND a.supplier_id=#{supplierId}</if>" +
            "<if test='status!=null and status!=\"\"'> AND a.status=#{status}</if>" +
            " ORDER BY a.id LIMIT #{offset},#{pageSize}</script>")
    List<CrmApDetail> page(@Param("supplierId") Long supplierId, @Param("status") String status,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM crm_ap_detail a WHERE a.deleted=0 " +
            "<if test='supplierId!=null'> AND a.supplier_id=#{supplierId}</if>" +
            "<if test='status!=null and status!=\"\"'> AND a.status=#{status}</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("status") String status);

    @Select("SELECT * FROM crm_ap_detail WHERE id=#{id} AND deleted=0")
    CrmApDetail findById(@Param("id") Long id);

    @Select("SELECT * FROM crm_ap_detail WHERE supplier_id=#{supplierId} AND status!='已结清' AND deleted=0 ORDER BY due_date")
    List<CrmApDetail> selectUnsettled(@Param("supplierId") Long supplierId);

    @Select("SELECT * FROM crm_ap_detail WHERE status!='已结清' AND deleted=0 ORDER BY due_date")
    List<CrmApDetail> selectAllUnsettled();

    @Insert("INSERT INTO crm_ap_detail(supplier_id,ref_type,ref_no,amount,paid,balance,status,due_date,create_time,create_by) " +
            "VALUES(#{supplierId},#{refType},#{refNo},#{amount},#{paid},#{balance},#{status},#{dueDate},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmApDetail d);

    @Update("UPDATE crm_ap_detail SET paid=paid+#{amount},balance=balance-#{amount}," +
            "status=CASE WHEN balance-#{amount}&lt;=0 THEN '已结清' WHEN paid+#{amount}>0 THEN '部分' ELSE '未结清' END,update_time=NOW() WHERE id=#{id}")
    int writeOff(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Select("SELECT COALESCE(SUM(balance),0) FROM crm_ap_detail WHERE supplier_id=#{supplierId} AND deleted=0")
    BigDecimal sumBalance(@Param("supplierId") Long supplierId);
}
