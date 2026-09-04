package com.erp.mapper;

import com.erp.entity.PurchaseOrder;
import com.erp.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PurchaseOrderMapper {

    @Select("<script>SELECT o.*, s.name AS supplierName, w.name AS warehouseName FROM purchase_order o " +
            "LEFT JOIN crm_supplier s ON o.supplier_id=s.id LEFT JOIN sys_warehouse w ON o.warehouse_id=w.id " +
            "WHERE o.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (o.order_no LIKE CONCAT('%',#{keyword},'%') OR s.name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='status!=null and status!=\"\"'> AND o.status=#{status}</if>" +
            " ORDER BY o.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<PurchaseOrder> page(@Param("keyword") String keyword, @Param("status") String status,
                             @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM purchase_order o LEFT JOIN crm_supplier s ON o.supplier_id=s.id " +
            "WHERE o.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (o.order_no LIKE CONCAT('%',#{keyword},'%') OR s.name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='status!=null and status!=\"\"'> AND o.status=#{status}</if></script>")
    long count(@Param("keyword") String keyword, @Param("status") String status);

    @Select("SELECT o.*, s.name AS supplierName, w.name AS warehouseName FROM purchase_order o " +
            "LEFT JOIN crm_supplier s ON o.supplier_id=s.id LEFT JOIN sys_warehouse w ON o.warehouse_id=w.id " +
            "WHERE o.id=#{id} AND o.deleted=0")
    PurchaseOrder findById(@Param("id") Long id);

    @Select("SELECT * FROM purchase_order WHERE order_no=#{orderNo} AND deleted=0")
    PurchaseOrder findByNo(@Param("orderNo") String orderNo);

    @Insert("INSERT INTO purchase_order(order_no,supplier_id,apply_date,warehouse_id,all_amount,tax_rate,tax_amount,status,audit_status,order_states,remark,create_time,create_by) " +
            "VALUES(#{orderNo},#{supplierId},#{applyDate},#{warehouseId},#{allAmount},#{taxRate},#{taxAmount},#{status},#{auditStatus},#{orderStates},#{remark},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseOrder o);

    @Update("UPDATE purchase_order SET supplier_id=#{supplierId},apply_date=#{applyDate},warehouse_id=#{warehouseId},all_amount=#{allAmount},tax_rate=#{taxRate}," +
            "tax_amount=#{taxAmount},remark=#{remark},update_time=NOW() WHERE id=#{id}")
    int update(PurchaseOrder o);

    @Update("UPDATE purchase_order SET status=#{status},audit_status=#{auditStatus},approve_person=#{approvePerson},approve_time=NOW(),order_states=#{orderStates},update_time=NOW() WHERE id=#{id}")
    int updateApprove(@Param("id") Long id, @Param("status") String status, @Param("auditStatus") String auditStatus,
                      @Param("approvePerson") String approvePerson, @Param("orderStates") String orderStates);

    @Update("UPDATE purchase_order SET vehicle_id=#{vehicleId},update_time=NOW() WHERE id=#{id}")
    int updateVehicle(@Param("id") Long id, @Param("vehicleId") Long vehicleId);

    @Update("UPDATE purchase_order SET warehouse_id=#{warehouseId},arrival_date=#{arrivalDate},status=#{status},order_states=#{orderStates},update_time=NOW() WHERE id=#{id}")
    int updateArrival(@Param("id") Long id, @Param("warehouseId") Long warehouseId,
                      @Param("arrivalDate") String arrivalDate, @Param("status") String status, @Param("orderStates") String orderStates);

    @Update("UPDATE purchase_order SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE purchase_order SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    // ---- 明细 ----
    @Select("SELECT i.*, g.name AS goodsName, g.code AS goodsCode, u.name AS unitName FROM purchase_order_item i " +
            "LEFT JOIN goods g ON i.goods_id=g.id LEFT JOIN goods_unit u ON g.unit_id=u.id WHERE i.order_id=#{orderId} AND i.deleted=0")
    List<PurchaseOrderItem> selectItems(@Param("orderId") Long orderId);

    @Insert("INSERT INTO purchase_order_item(order_id,goods_id,quantity,price,amount,received_qty,remark,create_time,create_by) " +
            "VALUES(#{orderId},#{goodsId},#{quantity},#{price},#{amount},#{receivedQty},#{remark},NOW(),#{createBy})")
    int insertItem(PurchaseOrderItem item);

    @Delete("DELETE FROM purchase_order_item WHERE order_id=#{orderId}")
    int deleteItems(@Param("orderId") Long orderId);

    @Update("UPDATE purchase_order_item SET received_qty=received_qty+#{qty},update_time=NOW() WHERE id=#{id}")
    int addReceivedQty(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Update("UPDATE purchase_order_item SET received_qty=#{receivedQty},update_time=NOW() WHERE id=#{id}")
    int setReceivedQty(@Param("id") Long id, @Param("receivedQty") BigDecimal receivedQty);
}
