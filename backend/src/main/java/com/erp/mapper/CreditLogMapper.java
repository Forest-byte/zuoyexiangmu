package com.erp.mapper;

import com.erp.entity.CrmCreditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreditLogMapper {

    @Select("SELECT * FROM crm_credit_log WHERE customer_id=#{customerId} AND deleted=0 ORDER BY operate_time DESC")
    List<CrmCreditLog> selectByCustomer(@Param("customerId") Long customerId);

    @Insert("INSERT INTO crm_credit_log(customer_id,change_amount,reason,operator,operate_time,create_time,create_by) " +
            "VALUES(#{customerId},#{changeAmount},#{reason},#{operator},#{operateTime},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmCreditLog log);
}
