package com.erp.mapper;

import com.erp.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE username=#{username} AND deleted=0")
    SysUser findByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE id=#{id} AND deleted=0")
    SysUser findById(@Param("id") Long id);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id=#{userId} AND deleted=0")
    List<Long> selectRoleIds(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user(username,password,name,role_code,dept_id,employee_id,status,create_time,create_by) " +
            "VALUES(#{username},#{password},#{name},#{roleCode},#{deptId},#{employeeId},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET username=#{username},name=#{name},role_code=#{roleCode},dept_id=#{deptId}," +
            "employee_id=#{employeeId},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(SysUser user);

    @Update("UPDATE sys_user SET password=#{password},update_time=NOW() WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE sys_user SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("<script>SELECT * FROM sys_user WHERE deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (username LIKE CONCAT('%',#{keyword},'%') OR name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY id LIMIT #{offset},#{pageSize}</script>")
    List<SysUser> page(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM sys_user WHERE deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (username LIKE CONCAT('%',#{keyword},'%') OR name LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM sys_user WHERE username=#{username} AND deleted=0")
    int countByUsername(@Param("username") String username);

    @Delete("DELETE FROM sys_user_role WHERE user_id=#{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role(user_id,role_id,create_time,create_by) VALUES(#{userId},#{roleId},NOW(),'system')")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
