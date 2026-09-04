package com.erp.mapper;

import com.erp.entity.FinTransfer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FinTransferMapper {

    @Select("SELECT t.*, a1.name AS fromName, a2.name AS toName FROM fin_transfer t " +
            "LEFT JOIN fin_account a1 ON t.from_account=a1.id LEFT JOIN fin_account a2 ON t.to_account=a2.id " +
            "WHERE t.deleted=0 ORDER BY t.id DESC")
    List<FinTransfer> selectAll();

    @Select("SELECT * FROM fin_transfer WHERE id=#{id} AND deleted=0")
    FinTransfer findById(@Param("id") Long id);

    @Insert("INSERT INTO fin_transfer(transfer_no,from_account,to_account,amount,status,applicant,apply_time,create_time,create_by) " +
            "VALUES(#{transferNo},#{fromAccount},#{toAccount},#{amount},#{status},#{applicant},NOW(),NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FinTransfer t);

    @Update("UPDATE fin_transfer SET status=#{status},update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE fin_transfer SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
