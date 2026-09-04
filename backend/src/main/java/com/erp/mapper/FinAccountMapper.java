package com.erp.mapper;

import com.erp.entity.FinAccount;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FinAccountMapper {

    @Select("SELECT * FROM fin_account WHERE deleted=0 ORDER BY id")
    List<FinAccount> selectAll();

    @Select("SELECT * FROM fin_account WHERE id=#{id} AND deleted=0")
    FinAccount findById(@Param("id") Long id);

    @Insert("INSERT INTO fin_account(name,account_no,begin_balance,balance,bank,status,create_time,create_by) " +
            "VALUES(#{name},#{accountNo},#{beginBalance},#{balance},#{bank},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FinAccount a);

    @Update("UPDATE fin_account SET name=#{name},account_no=#{accountNo},bank=#{bank},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(FinAccount a);

    @Update("UPDATE fin_account SET balance=balance+#{delta},update_time=NOW() WHERE id=#{id}")
    int changeBalance(@Param("id") Long id, @Param("delta") BigDecimal delta);

    @Update("UPDATE fin_account SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
