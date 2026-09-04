package com.erp.mapper;

import com.erp.entity.SysDict;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DictMapper {

    @Select("SELECT * FROM sys_dict WHERE deleted=0 ORDER BY sort,id")
    List<SysDict> selectAll();

    @Select("SELECT * FROM sys_dict WHERE dict_type=#{dictType} AND deleted=0 ORDER BY sort,id")
    List<SysDict> selectByType(@Param("dictType") String dictType);

    @Select("SELECT DISTINCT dict_type FROM sys_dict WHERE deleted=0")
    List<String> selectTypes();

    @Insert("INSERT INTO sys_dict(dict_type,label,value,sort,create_time,create_by) VALUES(#{dictType},#{label},#{value},#{sort},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysDict d);

    @Update("UPDATE sys_dict SET dict_type=#{dictType},label=#{label},value=#{value},sort=#{sort},update_time=NOW() WHERE id=#{id}")
    int update(SysDict d);

    @Update("UPDATE sys_dict SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
