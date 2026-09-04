package com.erp.mapper;

import com.erp.entity.JobTask;
import com.erp.entity.JobTaskLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JobTaskMapper {

    @Select("SELECT * FROM job_task WHERE deleted=0 ORDER BY id")
    List<JobTask> selectAll();

    @Select("SELECT * FROM job_task WHERE id=#{id} AND deleted=0")
    JobTask findById(@Param("id") Long id);

    @Select("SELECT * FROM job_task WHERE job_code=#{jobCode} AND deleted=0")
    JobTask findByCode(@Param("jobCode") String jobCode);

    @Select("SELECT * FROM job_task WHERE enabled=1 AND deleted=0")
    List<JobTask> selectEnabled();

    @Insert("INSERT INTO job_task(job_code,job_name,cron_expr,job_group,enabled,description,create_time,create_by) " +
            "VALUES(#{jobCode},#{jobName},#{cronExpr},#{jobGroup},#{enabled},#{description},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobTask t);

    @Update("UPDATE job_task SET job_name=#{jobName},cron_expr=#{cronExpr},job_group=#{jobGroup},enabled=#{enabled},description=#{description},update_time=NOW() WHERE id=#{id}")
    int update(JobTask t);

    @Update("UPDATE job_task SET enabled=#{enabled},update_time=NOW() WHERE id=#{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    @Update("UPDATE job_task SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Update("UPDATE job_task SET last_run_time=#{time},update_time=NOW() WHERE id=#{id}")
    int updateLastRun(@Param("id") Long id, @Param("time") LocalDateTime time);

    // ---- 日志 ----
    @Select("<script>SELECT l.*, t.job_name AS jobName, t.job_code AS jobCode FROM job_task_log l " +
            "LEFT JOIN job_task t ON l.job_id=t.id WHERE l.deleted=0 " +
            "<if test='jobId!=null'> AND l.job_id=#{jobId}</if>" +
            "<if test='result!=null and result!=\"\"'> AND l.result=#{result}</if>" +
            " ORDER BY l.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<JobTaskLog> pageLogs(@Param("jobId") Long jobId, @Param("result") String result,
                              @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM job_task_log l WHERE l.deleted=0 " +
            "<if test='jobId!=null'> AND l.job_id=#{jobId}</if>" +
            "<if test='result!=null and result!=\"\"'> AND l.result=#{result}</if></script>")
    long countLogs(@Param("jobId") Long jobId, @Param("result") String result);

    @Insert("INSERT INTO job_task_log(job_id,start_time,end_time,result,message,operator,create_time,create_by) " +
            "VALUES(#{jobId},#{startTime},#{endTime},#{result},#{message},#{operator},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLog(JobTaskLog log);
}
