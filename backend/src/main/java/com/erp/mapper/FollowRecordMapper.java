package com.erp.mapper;

import com.erp.entity.CrmFollowRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowRecordMapper {

    @Select("SELECT * FROM crm_follow_record WHERE customer_id=#{customerId} AND deleted=0 ORDER BY record_time DESC")
    List<CrmFollowRecord> selectByCustomer(@Param("customerId") Long customerId);

    @Insert("INSERT INTO crm_follow_record(customer_id,content,next_time,recorder,record_time,create_time,create_by) " +
            "VALUES(#{customerId},#{content},#{nextTime},#{recorder},#{recordTime},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmFollowRecord r);

    @Update("UPDATE crm_follow_record SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Update("UPDATE crm_follow_record SET customer_id=#{toId},update_time=NOW() WHERE customer_id=#{fromId} AND deleted=0")
    int merge(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Select("SELECT * FROM crm_follow_record WHERE next_time IS NOT NULL AND next_time &lt; CURDATE() AND deleted=0")
    List<CrmFollowRecord> selectOverdue();
}
