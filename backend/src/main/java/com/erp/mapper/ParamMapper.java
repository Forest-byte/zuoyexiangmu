package com.erp.mapper;

import com.erp.entity.SysParam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ParamMapper {

    @Select("SELECT * FROM sys_param WHERE deleted=0 ORDER BY id")
    List<SysParam> selectAll();

    @Select("SELECT * FROM sys_param WHERE param_key=#{key} AND deleted=0")
    SysParam findByKey(@Param("key") String key);

    @Insert("INSERT INTO sys_param(param_key,param_value,description,create_time,create_by) VALUES(#{paramKey},#{paramValue},#{description},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysParam p);

    @Update("UPDATE sys_param SET param_key=#{paramKey},param_value=#{paramValue},description=#{description},update_time=NOW() WHERE id=#{id}")
    int update(SysParam p);

    @Update("UPDATE sys_param SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
