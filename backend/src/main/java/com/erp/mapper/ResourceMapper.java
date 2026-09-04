package com.erp.mapper;

import com.erp.entity.SysResource;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResourceMapper {

    @Select("SELECT * FROM sys_resource WHERE deleted=0 ORDER BY sort,id")
    List<SysResource> selectAll();

    @Select("SELECT * FROM sys_resource WHERE id=#{id} AND deleted=0")
    SysResource findById(@Param("id") Long id);

    @Select("SELECT DISTINCT r.code FROM sys_resource r JOIN sys_role_resource rr ON r.id=rr.resource_id WHERE rr.role_id=#{roleId} AND r.code IS NOT NULL AND r.deleted=0")
    List<String> selectPermCodesByRole(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_resource(parent_id,name,type,code,path,icon,sort,create_time,create_by) " +
            "VALUES(#{parentId},#{name},#{type},#{code},#{path},#{icon},#{sort},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysResource res);

    @Update("UPDATE sys_resource SET parent_id=#{parentId},name=#{name},type=#{type},code=#{code},path=#{path},icon=#{icon},sort=#{sort},update_time=NOW() WHERE id=#{id}")
    int update(SysResource res);

    @Update("UPDATE sys_resource SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_resource WHERE parent_id=#{id} AND deleted=0")
    int countChildren(@Param("id") Long id);
}
