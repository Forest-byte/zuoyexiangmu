package com.erp.mapper;

import com.erp.entity.SysRegion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RegionMapper {

    @Select("SELECT * FROM sys_region WHERE deleted=0 ORDER BY sort,id")
    List<SysRegion> selectAll();

    @Insert("INSERT INTO sys_region(name,parent_id,sort,create_time,create_by) VALUES(#{name},#{parentId},#{sort},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRegion r);

    @Update("UPDATE sys_region SET name=#{name},parent_id=#{parentId},sort=#{sort},update_time=NOW() WHERE id=#{id}")
    int update(SysRegion r);

    @Update("UPDATE sys_region SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_region WHERE parent_id=#{id} AND deleted=0")
    int countChildren(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_company WHERE region_id=#{id} AND deleted=0")
    int countCompanyRef(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_region WHERE parent_id=#{parentId} AND name=#{name} AND deleted=0 AND id!=#{id}")
    int countSibling(@Param("parentId") Long parentId, @Param("name") String name, @Param("id") Long id);
}
