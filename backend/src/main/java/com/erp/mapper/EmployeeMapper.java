package com.erp.mapper;

import com.erp.entity.SysEmployee;
import com.erp.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    @Select("<script>SELECT * FROM sys_employee WHERE deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='deptId!=null'> AND dept_id=#{deptId}</if>" +
            " ORDER BY id LIMIT #{offset},#{pageSize}</script>")
    List<SysEmployee> page(@Param("keyword") String keyword, @Param("deptId") Long deptId,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM sys_employee WHERE deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (name LIKE CONCAT('%',#{keyword},'%') OR code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='deptId!=null'> AND dept_id=#{deptId}</if></script>")
    long count(@Param("keyword") String keyword, @Param("deptId") Long deptId);

    @Select("SELECT * FROM sys_employee WHERE id=#{id} AND deleted=0")
    SysEmployee findById(@Param("id") Long id);

    @Select("SELECT * FROM sys_employee WHERE deleted=0 ORDER BY id")
    List<SysEmployee> selectAll();

    @Insert("INSERT INTO sys_employee(name,code,dept_id,position,phone,email,status,create_time,create_by) " +
            "VALUES(#{name},#{code},#{deptId},#{position},#{phone},#{email},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysEmployee e);

    @Update("UPDATE sys_employee SET name=#{name},code=#{code},dept_id=#{deptId},position=#{position},phone=#{phone},email=#{email},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(SysEmployee e);

    @Update("UPDATE sys_employee SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Delete("DELETE FROM sys_user_role WHERE user_id=(SELECT id FROM sys_user WHERE employee_id=#{employeeId} AND deleted=0) AND deleted=0")
    int deleteUserRolesByEmployee(@Param("employeeId") Long employeeId);

    @Select("SELECT id FROM sys_user WHERE employee_id=#{employeeId} AND deleted=0")
    Long findUserIdByEmployee(@Param("employeeId") Long employeeId);

    @Insert("INSERT INTO sys_user_role(user_id,role_id,create_time,create_by) VALUES(#{userId},#{roleId},NOW(),'system')")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_user(username,password,name,role_code,dept_id,employee_id,status,create_time,create_by) " +
            "VALUES(#{username},#{password},#{name},#{roleCode},#{deptId},#{employeeId},#{status},NOW(),'system')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUserAccount(SysUser u);

    @Update("UPDATE sys_user SET username=#{username},name=#{name},dept_id=#{deptId},update_time=NOW() WHERE id=#{id}")
    int syncUserForUpdate(SysUser u);
}
