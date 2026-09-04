package com.erp.mapper;

import com.erp.entity.CrmCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM crm_category WHERE deleted=0 ORDER BY id")
    List<CrmCategory> selectAll();

    @Select("SELECT * FROM crm_category WHERE kind=#{kind} AND deleted=0 ORDER BY id")
    List<CrmCategory> selectByKind(@Param("kind") String kind);

    @Insert("INSERT INTO crm_category(parent_id,name,kind,create_time,create_by) VALUES(#{parentId},#{name},#{kind},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmCategory c);

    @Update("UPDATE crm_category SET parent_id=#{parentId},name=#{name},kind=#{kind},update_time=NOW() WHERE id=#{id}")
    int update(CrmCategory c);

    @Update("UPDATE crm_category SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM crm_category WHERE parent_id=#{id} AND deleted=0")
    int countChildren(@Param("id") Long id);
}
