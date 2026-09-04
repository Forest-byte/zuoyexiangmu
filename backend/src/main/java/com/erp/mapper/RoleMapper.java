package com.erp.mapper;

import com.erp.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT * FROM sys_role WHERE deleted=0 ORDER BY id")
    List<SysRole> selectAll();

    @Select("SELECT * FROM sys_role WHERE id=#{id} AND deleted=0")
    SysRole findById(@Param("id") Long id);

    @Select("SELECT * FROM sys_role WHERE role_code=#{roleCode} AND deleted=0")
    SysRole findByCode(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO sys_role(role_code,name,description,create_time,create_by) VALUES(#{roleCode},#{name},#{description},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    @Update("UPDATE sys_role SET role_code=#{roleCode},name=#{name},description=#{description},update_time=NOW() WHERE id=#{id}")
    int update(SysRole role);

    @Update("UPDATE sys_role SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT r.* FROM sys_role r JOIN sys_user_role ur ON r.id=ur.role_id WHERE ur.user_id=#{userId} AND r.deleted=0")
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sys_user_role WHERE role_id=#{roleId} AND deleted=0")
    int countUserByRole(@Param("roleId") Long roleId);

    @Select("SELECT role_id FROM sys_role_resource WHERE role_id=#{roleId} AND deleted=0")
    List<Long> selectResourceIds(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_resource WHERE role_id=#{roleId}")
    int deleteRoleResources(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_resource(role_id,resource_id,create_time,create_by) VALUES(#{roleId},#{resourceId},NOW(),'system')")
    int insertRoleResource(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
}
