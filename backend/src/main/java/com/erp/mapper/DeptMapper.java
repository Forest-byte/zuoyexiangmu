package com.erp.mapper;

import com.erp.entity.SysDept;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeptMapper {

    @Select("SELECT * FROM sys_dept WHERE deleted=0 ORDER BY id")
    List<SysDept> selectAll();

    @Select("SELECT * FROM sys_dept WHERE company_id=#{companyId} AND deleted=0 ORDER BY id")
    List<SysDept> selectByCompany(@Param("companyId") Long companyId);

    @Insert("INSERT INTO sys_dept(name,code,company_id,manager,phone,create_time,create_by) VALUES(#{name},#{code},#{companyId},#{manager},#{phone},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysDept d);

    @Update("UPDATE sys_dept SET name=#{name},code=#{code},company_id=#{companyId},manager=#{manager},phone=#{phone},update_time=NOW() WHERE id=#{id}")
    int update(SysDept d);

    @Update("UPDATE sys_dept SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_employee WHERE dept_id=#{id} AND deleted=0")
    int countEmployeeRef(@Param("id") Long id);
}
