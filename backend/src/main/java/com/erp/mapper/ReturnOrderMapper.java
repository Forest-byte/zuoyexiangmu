package com.erp.mapper;

import com.erp.entity.ReturnOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReturnOrderMapper {

    @Select("<script>SELECT r.* FROM return_order r WHERE r.deleted=0 " +
            "<if test='srcType!=null and srcType!=\"\"'> AND r.src_type=#{srcType}</if>" +
            "<if test='status!=null and status!=\"\"'> AND r.status=#{status}</if>" +
            " ORDER BY r.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<ReturnOrder> page(@Param("srcType") String srcType, @Param("status") String status,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM return_order r WHERE r.deleted=0 " +
            "<if test='srcType!=null and srcType!=\"\"'> AND r.src_type=#{srcType}</if>" +
            "<if test='status!=null and status!=\"\"'> AND r.status=#{status}</if></script>")
    long count(@Param("srcType") String srcType, @Param("status") String status);

    @Select("SELECT * FROM return_order WHERE id=#{id} AND deleted=0")
    ReturnOrder findById(@Param("id") Long id);

    @Insert("INSERT INTO return_order(return_no,src_type,src_id,partner_id,reason,amount,status,return_date,create_time,create_by) " +
            "VALUES(#{returnNo},#{srcType},#{srcId},#{partnerId},#{reason},#{amount},#{status},#{returnDate},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReturnOrder r);

    @Update("UPDATE return_order SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE return_order SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
