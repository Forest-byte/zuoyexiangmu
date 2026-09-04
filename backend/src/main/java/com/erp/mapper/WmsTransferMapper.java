package com.erp.mapper;

import com.erp.entity.WmsTransfer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WmsTransferMapper {

    @Select("<script>SELECT t.*, w1.name AS fromName, w2.name AS toName, g.name AS goodsName FROM wms_transfer t " +
            "LEFT JOIN sys_warehouse w1 ON t.from_warehouse=w1.id LEFT JOIN sys_warehouse w2 ON t.to_warehouse=w2.id " +
            "LEFT JOIN goods g ON t.goods_id=g.id WHERE t.deleted=0 " +
            "<if test='status!=null and status!=\"\"'> AND t.status=#{status}</if>" +
            " ORDER BY t.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<WmsTransfer> page(@Param("status") String status, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM wms_transfer t WHERE t.deleted=0 " +
            "<if test='status!=null and status!=\"\"'> AND t.status=#{status}</if></script>")
    long count(@Param("status") String status);

    @Select("SELECT * FROM wms_transfer WHERE id=#{id} AND deleted=0")
    WmsTransfer findById(@Param("id") Long id);

    @Insert("INSERT INTO wms_transfer(transfer_no,from_warehouse,to_warehouse,goods_id,quantity,status,applicant,apply_time,create_time,create_by) " +
            "VALUES(#{transferNo},#{fromWarehouse},#{toWarehouse},#{goodsId},#{quantity},#{status},#{applicant},NOW(),NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WmsTransfer t);

    @Update("UPDATE wms_transfer SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE wms_transfer SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
