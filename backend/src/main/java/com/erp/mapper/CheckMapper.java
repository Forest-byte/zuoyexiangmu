package com.erp.mapper;

import com.erp.entity.WmsCheck;
import com.erp.entity.WmsCheckItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CheckMapper {

    @Select("<script>SELECT c.*, w.name AS warehouseName FROM wms_check c LEFT JOIN sys_warehouse w ON c.warehouse_id=w.id " +
            "WHERE c.deleted=0 " +
            "<if test='status!=null and status!=\"\"'> AND c.status=#{status}</if>" +
            " ORDER BY c.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<WmsCheck> page(@Param("status") String status, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM wms_check c WHERE c.deleted=0 " +
            "<if test='status!=null and status!=\"\"'> AND c.status=#{status}</if></script>")
    long count(@Param("status") String status);

    @Select("SELECT c.*, w.name AS warehouseName FROM wms_check c LEFT JOIN sys_warehouse w ON c.warehouse_id=w.id WHERE c.id=#{id} AND c.deleted=0")
    WmsCheck findById(@Param("id") Long id);

    @Insert("INSERT INTO wms_check(check_no,warehouse_id,check_date,status,checker,create_time,create_by) " +
            "VALUES(#{checkNo},#{warehouseId},#{checkDate},#{status},#{checker},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WmsCheck c);

    @Update("UPDATE wms_check SET status=#{status},checker=#{checker},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("checker") String checker);

    @Update("UPDATE wms_check SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    // ---- 明细 ----
    @Select("SELECT i.*, g.name AS goodsName, g.code AS goodsCode, u.name AS unitName FROM wms_check_item i " +
            "LEFT JOIN goods g ON i.goods_id=g.id LEFT JOIN goods_unit u ON g.unit_id=u.id WHERE i.check_id=#{checkId} AND i.deleted=0")
    List<WmsCheckItem> selectItems(@Param("checkId") Long checkId);

    @Insert("INSERT INTO wms_check_item(check_id,goods_id,book_qty,real_qty,diff_qty,create_time,create_by) " +
            "VALUES(#{checkId},#{goodsId},#{bookQty},#{realQty},#{diffQty},NOW(),#{createBy})")
    int insertItem(WmsCheckItem item);

    @Update("UPDATE wms_check_item SET real_qty=#{realQty},diff_qty=#{diffQty},update_time=NOW() WHERE id=#{id}")
    int updateItemReal(WmsCheckItem item);

    @Delete("DELETE FROM wms_check_item WHERE check_id=#{checkId}")
    int deleteItems(@Param("checkId") Long checkId);
}
