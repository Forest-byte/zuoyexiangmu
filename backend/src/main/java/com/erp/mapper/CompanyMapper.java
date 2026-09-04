package com.erp.mapper;

import com.erp.entity.SysCompany;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompanyMapper {

    @Select("SELECT * FROM sys_company WHERE deleted=0 ORDER BY id")
    List<SysCompany> selectAll();

    @Insert("INSERT INTO sys_company(name,code,region_id,address,phone,status,create_time,create_by) " +
            "VALUES(#{name},#{code},#{regionId},#{address},#{phone},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysCompany c);

    @Update("UPDATE sys_company SET name=#{name},code=#{code},region_id=#{regionId},address=#{address},phone=#{phone},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(SysCompany c);

    @Update("UPDATE sys_company SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_dept WHERE company_id=#{id} AND deleted=0")
    int countDeptRef(@Param("id") Long id);
}
