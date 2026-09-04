package com.erp.mapper;

import com.erp.entity.SaleOrder;
import com.erp.entity.SaleOrderItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SaleOrderMapper {

    @Select("<script>SELECT o.*, c.name AS customerName, w.name AS warehouseName FROM sale_order o " +
            "LEFT JOIN crm_customer c ON o.customer_id=c.id LEFT JOIN sys_warehouse w ON o.warehouse_id=w.id " +
            "WHERE o.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (o.order_no LIKE CONCAT('%',#{keyword},'%') OR c.name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='status!=null and status!=\"\"'> AND o.status=#{status}</if>" +
            " ORDER BY o.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<SaleOrder> page(@Param("keyword") String keyword, @Param("status") String status,
                         @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM sale_order o LEFT JOIN crm_customer c ON o.customer_id=c.id " +
            "WHERE o.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (o.order_no LIKE CONCAT('%',#{keyword},'%') OR c.name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='status!=null and status!=\"\"'> AND o.status=#{status}</if></script>")
    long count(@Param("keyword") String keyword, @Param("status") String status);

    @Select("SELECT o.*, c.name AS customerName, w.name AS warehouseName FROM sale_order o " +
            "LEFT JOIN crm_customer c ON o.customer_id=c.id LEFT JOIN sys_warehouse w ON o.warehouse_id=w.id " +
            "WHERE o.id=#{id} AND o.deleted=0")
    SaleOrder findById(@Param("id") Long id);

    @Insert("INSERT INTO sale_order(order_no,customer_id,order_date,all_amount,discount,received_amount,status,audit_status,warehouse_id,delivery_date,settle_status,remark,create_time,create_by) " +
            "VALUES(#{orderNo},#{customerId},#{orderDate},#{allAmount},#{discount},#{receivedAmount},#{status},#{auditStatus},#{warehouseId},#{deliveryDate},#{settleStatus},#{remark},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SaleOrder o);

    @Update("UPDATE sale_order SET customer_id=#{customerId},order_date=#{orderDate},all_amount=#{allAmount},discount=#{discount}," +
            "warehouse_id=#{warehouseId},delivery_date=#{deliveryDate},remark=#{remark},update_time=NOW() WHERE id=#{id}")
    int update(SaleOrder o);

    @Update("UPDATE sale_order SET status=#{status},audit_status=#{auditStatus},settle_person=#{settlePerson},update_time=NOW() WHERE id=#{id}")
    int updateApprove(@Param("id") Long id, @Param("status") String status, @Param("auditStatus") String auditStatus,
                      @Param("settlePerson") String settlePerson);

    @Update("UPDATE sale_order SET status=#{status},order_states=#{orderStates},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("orderStates") String orderStates);

    @Update("UPDATE sale_order SET received_amount=received_amount+#{amount},settle_status=#{settleStatus},update_time=NOW() WHERE id=#{id}")
    int addReceived(@Param("id") Long id, @Param("amount") BigDecimal amount, @Param("settleStatus") String settleStatus);

    @Update("UPDATE sale_order SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    // ---- 明细 ----
    @Select("SELECT i.*, g.name AS goodsName, g.code AS goodsCode, u.name AS unitName FROM sale_order_item i " +
            "LEFT JOIN goods g ON i.goods_id=g.id LEFT JOIN goods_unit u ON g.unit_id=u.id WHERE i.order_id=#{orderId} AND i.deleted=0")
    List<SaleOrderItem> selectItems(@Param("orderId") Long orderId);

    @Insert("INSERT INTO sale_order_item(order_id,goods_id,quantity,price,amount,delivered_qty,remark,create_time,create_by) " +
            "VALUES(#{orderId},#{goodsId},#{quantity},#{price},#{amount},#{deliveredQty},#{remark},NOW(),#{createBy})")
    int insertItem(SaleOrderItem item);

    @Delete("DELETE FROM sale_order_item WHERE order_id=#{orderId}")
    int deleteItems(@Param("orderId") Long orderId);

    @Update("UPDATE sale_order_item SET delivered_qty=delivered_qty+#{qty},update_time=NOW() WHERE id=#{id}")
    int addDeliveredQty(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Update("UPDATE sale_order_item SET delivered_qty=#{deliveredQty},update_time=NOW() WHERE id=#{id}")
    int setDeliveredQty(@Param("id") Long id, @Param("deliveredQty") BigDecimal deliveredQty);
}
