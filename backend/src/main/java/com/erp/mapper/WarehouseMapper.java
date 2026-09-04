package com.erp.mapper;

import com.erp.entity.SysWarehouse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WarehouseMapper {

    @Select("<script>SELECT * FROM sys_warehouse WHERE deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY id</script>")
    List<SysWarehouse> selectList(@Param("keyword") String keyword);

    @Select("SELECT * FROM sys_warehouse WHERE deleted=0 ORDER BY id")
    List<SysWarehouse> selectAll();

    @Select("SELECT * FROM sys_warehouse WHERE id=#{id} AND deleted=0")
    SysWarehouse findById(@Param("id") Long id);

    @Insert("INSERT INTO sys_warehouse(name,code,address,manager,phone,status,create_time,create_by) " +
            "VALUES(#{name},#{code},#{address},#{manager},#{phone},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysWarehouse w);

    @Update("UPDATE sys_warehouse SET name=#{name},code=#{code},address=#{address},manager=#{manager},phone=#{phone},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(SysWarehouse w);

    @Update("UPDATE sys_warehouse SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
