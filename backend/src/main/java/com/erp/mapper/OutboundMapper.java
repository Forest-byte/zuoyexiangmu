package com.erp.mapper;

import com.erp.entity.WmsOutbound;
import com.erp.entity.WmsOutboundItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OutboundMapper {

    @Select("<script>SELECT i.*, w.name AS warehouseName FROM wms_outbound i LEFT JOIN sys_warehouse w ON i.warehouse_id=w.id " +
            "WHERE i.deleted=0 " +
            "<if test='outType!=null and outType!=\"\"'> AND i.out_type=#{outType}</if>" +
            "<if test='keyword!=null and keyword!=\"\"'> AND (i.out_no LIKE CONCAT('%',#{keyword},'%') OR i.src_no LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY i.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<WmsOutbound> page(@Param("outType") String outType, @Param("keyword") String keyword,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM wms_outbound i WHERE i.deleted=0 " +
            "<if test='outType!=null and outType!=\"\"'> AND i.out_type=#{outType}</if>" +
            "<if test='keyword!=null and keyword!=\"\"'> AND (i.out_no LIKE CONCAT('%',#{keyword},'%') OR i.src_no LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("outType") String outType, @Param("keyword") String keyword);

    @Select("SELECT i.*, w.name AS warehouseName FROM wms_outbound i LEFT JOIN sys_warehouse w ON i.warehouse_id=w.id WHERE i.id=#{id} AND i.deleted=0")
    WmsOutbound findById(@Param("id") Long id);

    @Insert("INSERT INTO wms_outbound(out_no,out_type,src_no,warehouse_id,out_date,operator,status,create_time,create_by) " +
            "VALUES(#{outNo},#{outType},#{srcNo},#{warehouseId},#{outDate},#{operator},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WmsOutbound i);

    @Update("UPDATE wms_outbound SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    // ---- 明细 ----
    @Select("SELECT i.*, g.name AS goodsName, g.code AS goodsCode, u.name AS unitName FROM wms_outbound_item i " +
            "LEFT JOIN goods g ON i.goods_id=g.id LEFT JOIN goods_unit u ON g.unit_id=u.id WHERE i.outbound_id=#{outboundId} AND i.deleted=0")
    List<WmsOutboundItem> selectItems(@Param("outboundId") Long outboundId);

    @Insert("INSERT INTO wms_outbound_item(outbound_id,goods_id,quantity,create_time,create_by) VALUES(#{outboundId},#{goodsId},#{quantity},NOW(),#{createBy})")
    int insertItem(WmsOutboundItem item);

    @Delete("DELETE FROM wms_outbound_item WHERE outbound_id=#{outboundId}")
    int deleteItems(@Param("outboundId") Long outboundId);
}
