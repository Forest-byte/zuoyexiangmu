package com.erp.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RptSnapshotMapper {

    @Select("SELECT * FROM rpt_snapshot WHERE rpt_code=#{rptCode} AND biz_date=#{bizDate} AND deleted=0 LIMIT 1")
    Map<String, Object> findByCodeAndDate(@Param("rptCode") String rptCode, @Param("bizDate") String bizDate);

    @Select("SELECT * FROM rpt_snapshot WHERE rpt_code=#{rptCode} AND deleted=0 ORDER BY biz_date DESC LIMIT 1")
    Map<String, Object> findLatest(@Param("rptCode") String rptCode);

    @Insert("INSERT INTO rpt_snapshot(rpt_code,biz_date,content,generate_time,create_time,create_by) " +
            "VALUES(#{rptCode},#{bizDate},#{content},NOW(),NOW(),'job')")
    int insert(@Param("rptCode") String rptCode, @Param("bizDate") String bizDate, @Param("content") String content);

    @Delete("DELETE FROM rpt_snapshot WHERE rpt_code=#{rptCode} AND biz_date=#{bizDate}")
    int deleteByCodeAndDate(@Param("rptCode") String rptCode, @Param("bizDate") String bizDate);

    @Select("SELECT * FROM rpt_snapshot WHERE deleted=0 ORDER BY generate_time DESC LIMIT 20")
    List<Map<String, Object>> selectRecent();
}
