package com.erp.mapper;

import com.erp.entity.WmsStock;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StockMapper {

    @Select("<script>SELECT s.*, g.name AS goodsName, g.code AS goodsCode, g.low_limit AS lowLimit, g.status AS status, " +
            "c.name AS categoryName, w.name AS warehouseName FROM wms_stock s " +
            "LEFT JOIN goods g ON s.goods_id=g.id LEFT JOIN goods_category c ON g.category_id=c.id " +
            "LEFT JOIN sys_warehouse w ON s.warehouse_id=w.id WHERE s.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (g.name LIKE CONCAT('%',#{keyword},'%') OR g.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='warehouseId!=null'> AND s.warehouse_id=#{warehouseId}</if>" +
            "<if test='categoryId!=null'> AND g.category_id=#{categoryId}</if>" +
            " ORDER BY s.warehouse_id, s.goods_id LIMIT #{offset},#{pageSize}</script>")
    List<WmsStock> page(@Param("keyword") String keyword, @Param("warehouseId") Long warehouseId,
                        @Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM wms_stock s LEFT JOIN goods g ON s.goods_id=g.id WHERE s.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (g.name LIKE CONCAT('%',#{keyword},'%') OR g.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='warehouseId!=null'> AND s.warehouse_id=#{warehouseId}</if>" +
            "<if test='categoryId!=null'> AND g.category_id=#{categoryId}</if></script>")
    long count(@Param("keyword") String keyword, @Param("warehouseId") Long warehouseId, @Param("categoryId") Long categoryId);

    @Select("SELECT * FROM wms_stock WHERE warehouse_id=#{warehouseId} AND goods_id=#{goodsId} AND deleted=0")
    WmsStock find(@Param("warehouseId") Long warehouseId, @Param("goodsId") Long goodsId);

    @Insert("INSERT INTO wms_stock(warehouse_id,goods_id,quantity,unit,update_time,create_time,create_by) " +
            "VALUES(#{warehouseId},#{goodsId},#{quantity},#{unit},NOW(),NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WmsStock s);

    @Update("UPDATE wms_stock SET quantity=quantity+#{delta},update_time=NOW() WHERE warehouse_id=#{warehouseId} AND goods_id=#{goodsId} AND deleted=0")
    int change(@Param("warehouseId") Long warehouseId, @Param("goodsId") Long goodsId, @Param("delta") BigDecimal delta);

    @Update("UPDATE wms_stock SET quantity=#{qty},update_time=NOW() WHERE warehouse_id=#{warehouseId} AND goods_id=#{goodsId} AND deleted=0")
    int setQty(@Param("warehouseId") Long warehouseId, @Param("goodsId") Long goodsId, @Param("qty") BigDecimal qty);

    @Select("SELECT s.*, g.name AS goodsName, g.code AS goodsCode, g.low_limit AS lowLimit, w.name AS warehouseName FROM wms_stock s " +
            "LEFT JOIN goods g ON s.goods_id=g.id LEFT JOIN sys_warehouse w ON s.warehouse_id=w.id " +
            "WHERE s.deleted=0 AND g.status='在售' AND s.quantity &lt; g.low_limit")
    List<WmsStock> selectLowStock();

    @Select("SELECT * FROM wms_stock WHERE warehouse_id=#{warehouseId} AND deleted=0 ORDER BY goods_id")
    List<WmsStock> selectByWarehouse(@Param("warehouseId") Long warehouseId);

    @Select("SELECT COALESCE(SUM(quantity),0) FROM wms_stock WHERE goods_id=#{goodsId} AND deleted=0")
    BigDecimal sumQty(@Param("goodsId") Long goodsId);
}
