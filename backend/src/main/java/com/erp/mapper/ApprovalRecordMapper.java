package com.erp.mapper;

import com.erp.entity.ApprovalRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApprovalRecordMapper {

    @Select("SELECT * FROM approval_record WHERE doc_type=#{docType} AND doc_id=#{docId} AND deleted=0 ORDER BY id")
    List<ApprovalRecord> selectByDoc(@Param("docType") String docType, @Param("docId") Long docId);

    @Insert("INSERT INTO approval_record(doc_type,doc_id,level,approver,result,comment,approve_time,create_time,create_by) " +
            "VALUES(#{docType},#{docId},#{level},#{approver},#{result},#{comment},#{approveTime},NOW(),#{createBy})")
    int insert(ApprovalRecord r);
}
