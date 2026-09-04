package com.erp.mapper;

import com.erp.entity.FinAccountLog;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FinAccountLogMapper {

    @Select("<script>SELECT l.*, a.name AS accountName FROM fin_account_log l LEFT JOIN fin_account a ON l.account_id=a.id " +
            "WHERE l.deleted=0 " +
            "<if test='accountId!=null'> AND l.account_id=#{accountId}</if>" +
            "<if test='bizType!=null and bizType!=\"\"'> AND l.biz_type=#{bizType}</if>" +
            "<if test='refNo!=null and refNo!=\"\"'> AND l.ref_no LIKE CONCAT('%',#{refNo},'%')</if>" +
            "<if test='startDate!=null and startDate!=\"\"'> AND l.biz_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND l.biz_date&lt;=#{endDate}</if>" +
            " ORDER BY l.biz_date DESC, l.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<FinAccountLog> page(@Param("accountId") Long accountId, @Param("bizType") String bizType, @Param("refNo") String refNo,
                             @Param("startDate") String startDate, @Param("endDate") String endDate,
                             @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM fin_account_log l WHERE l.deleted=0 " +
            "<if test='accountId!=null'> AND l.account_id=#{accountId}</if>" +
            "<if test='bizType!=null and bizType!=\"\"'> AND l.biz_type=#{bizType}</if>" +
            "<if test='refNo!=null and refNo!=\"\"'> AND l.ref_no LIKE CONCAT('%',#{refNo},'%')</if>" +
            "<if test='startDate!=null and startDate!=\"\"'> AND l.biz_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND l.biz_date&lt;=#{endDate}</if></script>")
    long count(@Param("accountId") Long accountId, @Param("bizType") String bizType, @Param("refNo") String refNo,
               @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Insert("INSERT INTO fin_account_log(account_id,biz_type,ref_no,in_amount,out_amount,balance_after,biz_date,operator,create_time,create_by) " +
            "VALUES(#{accountId},#{bizType},#{refNo},#{inAmount},#{outAmount},#{balanceAfter},#{bizDate},#{operator},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FinAccountLog l);
}
