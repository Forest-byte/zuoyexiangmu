package com.erp.mapper;

import com.erp.entity.SysMeeting;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MeetingMapper {

    @Select("SELECT * FROM sys_meeting WHERE deleted=0 ORDER BY id")
    List<SysMeeting> selectAll();

    @Insert("INSERT INTO sys_meeting(name,capacity,status,create_time,create_by) VALUES(#{name},#{capacity},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMeeting m);

    @Update("UPDATE sys_meeting SET name=#{name},capacity=#{capacity},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(SysMeeting m);

    @Update("UPDATE sys_meeting SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
