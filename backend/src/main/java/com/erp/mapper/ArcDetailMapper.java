package com.erp.mapper;

import com.erp.entity.CrmArcDetail;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ArcDetailMapper {

    @Select("<script>SELECT a.*, c.name AS customerName FROM crm_arc_detail a LEFT JOIN crm_customer c ON a.customer_id=c.id " +
            "WHERE a.deleted=0 " +
            "<if test='customerId!=null'> AND a.customer_id=#{customerId}</if>" +
            "<if test='status!=null and status!=\"\"'> AND a.status=#{status}</if>" +
            " ORDER BY a.id LIMIT #{offset},#{pageSize}</script>")
    List<CrmArcDetail> page(@Param("customerId") Long customerId, @Param("status") String status,
                            @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM crm_arc_detail a WHERE a.deleted=0 " +
            "<if test='customerId!=null'> AND a.customer_id=#{customerId}</if>" +
            "<if test='status!=null and status!=\"\"'> AND a.status=#{status}</if></script>")
    long count(@Param("customerId") Long customerId, @Param("status") String status);

    @Select("SELECT * FROM crm_arc_detail WHERE id=#{id} AND deleted=0")
    CrmArcDetail findById(@Param("id") Long id);

    @Select("SELECT * FROM crm_arc_detail WHERE customer_id=#{customerId} AND status!='已结清' AND deleted=0 ORDER BY due_date")
    List<CrmArcDetail> selectUnsettled(@Param("customerId") Long customerId);

    @Select("SELECT * FROM crm_arc_detail WHERE status!='已结清' AND deleted=0 ORDER BY due_date")
    List<CrmArcDetail> selectAllUnsettled();

    @Insert("INSERT INTO crm_arc_detail(customer_id,ref_type,ref_no,amount,received,balance,status,due_date,create_time,create_by) " +
            "VALUES(#{customerId},#{refType},#{refNo},#{amount},#{received},#{balance},#{status},#{dueDate},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmArcDetail d);

    @Update("UPDATE crm_arc_detail SET received=received+#{amount},balance=balance-#{amount}," +
            "status=CASE WHEN balance-#{amount}&lt;=0 THEN '已结清' WHEN received+#{amount}>0 THEN '部分' ELSE '未结清' END,update_time=NOW() WHERE id=#{id}")
    int writeOff(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE crm_arc_detail SET customer_id=#{toId},update_time=NOW() WHERE customer_id=#{fromId} AND deleted=0")
    int merge(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Select("SELECT COALESCE(SUM(balance),0) FROM crm_arc_detail WHERE customer_id=#{customerId} AND deleted=0")
    BigDecimal sumBalance(@Param("customerId") Long customerId);

    /** 退货冲减：按来源单号优先冲减未结清应收 */
    @Update("UPDATE crm_arc_detail SET balance=balance-#{amount},received=received+#{amount}," +
            "status=CASE WHEN balance-#{amount}&lt;=0 THEN '已结清' ELSE status END,update_time=NOW() " +
            "WHERE ref_no=#{refNo} AND deleted=0 AND status!='已结清' ORDER BY due_date LIMIT 1")
    int returnRefund(@Param("refNo") String refNo, @Param("amount") BigDecimal amount);
}
