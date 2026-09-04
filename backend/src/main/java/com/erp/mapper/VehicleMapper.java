package com.erp.mapper;

import com.erp.entity.SysVehicle;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VehicleMapper {

    @Select("SELECT * FROM sys_vehicle WHERE deleted=0 ORDER BY id")
    List<SysVehicle> selectAll();

    @Insert("INSERT INTO sys_vehicle(name,code,type,capacity,driver,phone,status,create_time,create_by) " +
            "VALUES(#{name},#{code},#{type},#{capacity},#{driver},#{phone},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysVehicle v);

    @Update("UPDATE sys_vehicle SET name=#{name},code=#{code},type=#{type},capacity=#{capacity},driver=#{driver},phone=#{phone},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(SysVehicle v);

    @Update("UPDATE sys_vehicle SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE sys_vehicle SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
