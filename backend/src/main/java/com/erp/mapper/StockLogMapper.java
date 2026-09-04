package com.erp.mapper;

import com.erp.entity.WmsStockLog;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StockLogMapper {

    @Select("<script>SELECT l.*, g.name AS goodsName, w.name AS warehouseName FROM wms_stock_log l " +
            "LEFT JOIN goods g ON l.goods_id=g.id LEFT JOIN sys_warehouse w ON l.warehouse_id=w.id " +
            "WHERE l.deleted=0 " +
            "<if test='goodsId!=null'> AND l.goods_id=#{goodsId}</if>" +
            "<if test='warehouseId!=null'> AND l.warehouse_id=#{warehouseId}</if>" +
            "<if test='changeType!=null and changeType!=\"\"'> AND l.change_type=#{changeType}</if>" +
            "<if test='startTime!=null and startTime!=\"\"'> AND l.change_time&gt;=#{startTime}</if>" +
            "<if test='endTime!=null and endTime!=\"\"'> AND l.change_time&lt;=#{endTime}</if>" +
            " ORDER BY l.change_time DESC LIMIT #{offset},#{pageSize}</script>")
    List<WmsStockLog> page(@Param("goodsId") Long goodsId, @Param("warehouseId") Long warehouseId,
                           @Param("changeType") String changeType, @Param("startTime") String startTime,
                           @Param("endTime") String endTime, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM wms_stock_log l WHERE l.deleted=0 " +
            "<if test='goodsId!=null'> AND l.goods_id=#{goodsId}</if>" +
            "<if test='warehouseId!=null'> AND l.warehouse_id=#{warehouseId}</if>" +
            "<if test='changeType!=null and changeType!=\"\"'> AND l.change_type=#{changeType}</if>" +
            "<if test='startTime!=null and startTime!=\"\"'> AND l.change_time&gt;=#{startTime}</if>" +
            "<if test='endTime!=null and endTime!=\"\"'> AND l.change_time&lt;=#{endTime}</if></script>")
    long count(@Param("goodsId") Long goodsId, @Param("warehouseId") Long warehouseId, @Param("changeType") String changeType,
               @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Insert("INSERT INTO wms_stock_log(goods_id,warehouse_id,change_type,change_qty,before_qty,after_qty,ref_no,operator,change_time,create_time,create_by) " +
            "VALUES(#{goodsId},#{warehouseId},#{changeType},#{changeQty},#{beforeQty},#{afterQty},#{refNo},#{operator},NOW(),NOW(),#{createBy})")
    int insert(WmsStockLog l);
}
