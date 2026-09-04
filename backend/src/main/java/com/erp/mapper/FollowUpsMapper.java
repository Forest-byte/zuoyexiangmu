package com.erp.mapper;

import com.erp.entity.FollowUps;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowUpsMapper {

    @Select("SELECT * FROM follow_ups WHERE doc_type=#{docType} AND doc_id=#{docId} AND deleted=0 ORDER BY id")
    List<FollowUps> selectByDoc(@Param("docType") String docType, @Param("docId") Long docId);

    @Insert("INSERT INTO follow_ups(doc_type,doc_id,node_name,node_status,operator,operate_time,create_time,create_by) " +
            "VALUES(#{docType},#{docId},#{nodeName},#{nodeStatus},#{operator},#{operateTime},NOW(),#{createBy})")
    int insert(FollowUps f);

    @Update("UPDATE follow_ups SET node_status=#{nodeStatus},operator=#{operator},operate_time=#{operateTime},update_time=NOW() " +
            "WHERE doc_type=#{docType} AND doc_id=#{docId} AND node_name=#{nodeName} AND deleted=0")
    int updateNode(FollowUps f);
}
