package com.erp.mapper;

import com.erp.entity.PurchaseDemand;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PurchaseDemandMapper {

    @Select("<script>SELECT d.*, g.name AS goodsName FROM purchase_demand d LEFT JOIN goods g ON d.goods_id=g.id " +
            "WHERE d.deleted=0 " +
            "<if test='status!=null and status!=\"\"'> AND d.status=#{status}</if>" +
            " ORDER BY d.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<PurchaseDemand> page(@Param("status") String status, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM purchase_demand d WHERE d.deleted=0 " +
            "<if test='status!=null and status!=\"\"'> AND d.status=#{status}</if></script>")
    long count(@Param("status") String status);

    @Select("SELECT * FROM purchase_demand WHERE id IN (${ids}) AND deleted=0 AND status='待处理'")
    List<PurchaseDemand> selectByIds(@Param("ids") String ids);

    @Insert("INSERT INTO purchase_demand(demand_no,goods_id,quantity,note,need_date,applicant,status,create_time,create_by) " +
            "VALUES(#{demandNo},#{goodsId},#{quantity},#{note},#{needDate},#{applicant},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseDemand d);

    @Update("UPDATE purchase_demand SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE purchase_demand SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
