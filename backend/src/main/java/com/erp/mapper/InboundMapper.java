package com.erp.mapper;

import com.erp.entity.WmsInbound;
import com.erp.entity.WmsInboundItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InboundMapper {

    @Select("<script>SELECT i.*, w.name AS warehouseName FROM wms_inbound i LEFT JOIN sys_warehouse w ON i.warehouse_id=w.id " +
            "WHERE i.deleted=0 " +
            "<if test='inType!=null and inType!=\"\"'> AND i.in_type=#{inType}</if>" +
            "<if test='keyword!=null and keyword!=\"\"'> AND (i.in_no LIKE CONCAT('%',#{keyword},'%') OR i.src_no LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY i.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<WmsInbound> page(@Param("inType") String inType, @Param("keyword") String keyword,
                          @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM wms_inbound i WHERE i.deleted=0 " +
            "<if test='inType!=null and inType!=\"\"'> AND i.in_type=#{inType}</if>" +
            "<if test='keyword!=null and keyword!=\"\"'> AND (i.in_no LIKE CONCAT('%',#{keyword},'%') OR i.src_no LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("inType") String inType, @Param("keyword") String keyword);

    @Select("SELECT i.*, w.name AS warehouseName FROM wms_inbound i LEFT JOIN sys_warehouse w ON i.warehouse_id=w.id WHERE i.id=#{id} AND i.deleted=0")
    WmsInbound findById(@Param("id") Long id);

    @Insert("INSERT INTO wms_inbound(in_no,in_type,src_no,warehouse_id,in_date,total_amount,operator,status,create_time,create_by) " +
            "VALUES(#{inNo},#{inType},#{srcNo},#{warehouseId},#{inDate},#{totalAmount},#{operator},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WmsInbound i);

    @Update("UPDATE wms_inbound SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE wms_inbound SET total_amount=#{total},update_time=NOW() WHERE id=#{id}")
    int updateTotal(@Param("id") Long id, @Param("total") java.math.BigDecimal total);

    @Update("UPDATE wms_inbound SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT * FROM wms_inbound WHERE src_no=#{srcNo} AND deleted=0")
    List<WmsInbound> selectBySrc(@Param("srcNo") String srcNo);

    // ---- 明细 ----
    @Select("SELECT i.*, g.name AS goodsName, g.code AS goodsCode, u.name AS unitName FROM wms_inbound_item i " +
            "LEFT JOIN goods g ON i.goods_id=g.id LEFT JOIN goods_unit u ON g.unit_id=u.id WHERE i.inbound_id=#{inboundId} AND i.deleted=0")
    List<WmsInboundItem> selectItems(@Param("inboundId") Long inboundId);

    @Insert("INSERT INTO wms_inbound_item(inbound_id,goods_id,quantity,price,amount,create_time,create_by) " +
            "VALUES(#{inboundId},#{goodsId},#{quantity},#{price},#{amount},NOW(),#{createBy})")
    int insertItem(WmsInboundItem item);

    @Delete("DELETE FROM wms_inbound_item WHERE inbound_id=#{inboundId}")
    int deleteItems(@Param("inboundId") Long inboundId);
}
