package com.erp.mapper;

import com.erp.entity.ApprovalRule;
import com.erp.entity.SysCodeRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CodeRuleMapper {

    @Select("SELECT * FROM sys_code_rule WHERE deleted=0 ORDER BY id")
    List<SysCodeRule> selectAll();

    @Select("SELECT * FROM sys_code_rule WHERE doc_type=#{docType} AND deleted=0")
    SysCodeRule findByDocType(@Param("docType") String docType);

    @Insert("INSERT INTO sys_code_rule(doc_type,prefix,format,seq_len,create_time,create_by) VALUES(#{docType},#{prefix},#{format},#{seqLen},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysCodeRule r);

    @Update("UPDATE sys_code_rule SET doc_type=#{docType},prefix=#{prefix},format=#{format},seq_len=#{seqLen},update_time=NOW() WHERE id=#{id}")
    int update(SysCodeRule r);

    @Update("UPDATE sys_code_rule SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    // ---- 审批规则 ----
    @Select("SELECT * FROM approval_rule WHERE deleted=0 ORDER BY id")
    List<ApprovalRule> selectApprovalAll();

    @Select("SELECT * FROM approval_rule WHERE doc_type=#{docType} AND deleted=0")
    List<ApprovalRule> selectApprovalByType(@Param("docType") String docType);

    @Insert("INSERT INTO approval_rule(doc_type,role_code,level,enabled,create_time,create_by) VALUES(#{docType},#{roleCode},#{level},#{enabled},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertApproval(ApprovalRule r);

    @Update("UPDATE approval_rule SET doc_type=#{docType},role_code=#{roleCode},level=#{level},enabled=#{enabled},update_time=NOW() WHERE id=#{id}")
    int updateApproval(ApprovalRule r);

    @Update("UPDATE approval_rule SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int deleteApproval(@Param("id") Long id);
}
