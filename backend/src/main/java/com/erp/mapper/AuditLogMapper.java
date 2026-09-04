package com.erp.mapper;

import com.erp.entity.SysAuditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO sys_audit_log(operator,action,target,before_data,after_data,time,create_time,create_by) " +
            "VALUES(#{operator},#{action},#{target},#{beforeData},#{afterData},#{time},NOW(),'system')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysAuditLog log);

    @Select("<script>SELECT * FROM sys_audit_log WHERE deleted=0 " +
            "<if test='operator!=null and operator!=\"\"'> AND operator LIKE CONCAT('%',#{operator},'%')</if>" +
            "<if test='action!=null and action!=\"\"'> AND action LIKE CONCAT('%',#{action},'%')</if>" +
            "<if test='startTime!=null'> AND time&gt;=#{startTime}</if>" +
            "<if test='endTime!=null'> AND time&lt;=#{endTime}</if>" +
            " ORDER BY time DESC LIMIT #{offset},#{pageSize}</script>")
    List<SysAuditLog> page(@Param("operator") String operator, @Param("action") String action,
                           @Param("startTime") String startTime, @Param("endTime") String endTime,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM sys_audit_log WHERE deleted=0 " +
            "<if test='operator!=null and operator!=\"\"'> AND operator LIKE CONCAT('%',#{operator},'%')</if>" +
            "<if test='action!=null and action!=\"\"'> AND action LIKE CONCAT('%',#{action},'%')</if>" +
            "<if test='startTime!=null'> AND time&gt;=#{startTime}</if>" +
            "<if test='endTime!=null'> AND time&lt;=#{endTime}</if></script>")
    long count(@Param("operator") String operator, @Param("action") String action,
               @Param("startTime") String startTime, @Param("endTime") String endTime);
}
