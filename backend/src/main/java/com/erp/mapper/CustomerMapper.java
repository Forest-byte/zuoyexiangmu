package com.erp.mapper;

import com.erp.entity.CrmCustomer;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CustomerMapper {

    @Select("<script>SELECT c.*, cat.name AS categoryName FROM crm_customer c LEFT JOIN crm_category cat ON c.category_id=cat.id " +
            "WHERE c.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (c.name LIKE CONCAT('%',#{keyword},'%') OR c.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='status!=null and status!=\"\"'> AND c.status=#{status}</if>" +
            " ORDER BY c.id LIMIT #{offset},#{pageSize}</script>")
    List<CrmCustomer> page(@Param("keyword") String keyword, @Param("status") String status,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM crm_customer c WHERE c.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (c.name LIKE CONCAT('%',#{keyword},'%') OR c.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='status!=null and status!=\"\"'> AND c.status=#{status}</if></script>")
    long count(@Param("keyword") String keyword, @Param("status") String status);

    @Select("SELECT * FROM crm_customer WHERE deleted=0 ORDER BY id")
    List<CrmCustomer> selectAll();

    @Select("SELECT * FROM crm_customer WHERE id=#{id} AND deleted=0")
    CrmCustomer findById(@Param("id") Long id);

    @Select("SELECT MAX(id) FROM crm_customer WHERE deleted=0")
    Long maxId();

    @Insert("INSERT INTO crm_customer(code,name,category_id,linkman,phone,address,credit_limit,used_credit,debt_amount,status,approval_status,create_time,create_by) " +
            "VALUES(#{code},#{name},#{categoryId},#{linkman},#{phone},#{address},#{creditLimit},#{usedCredit},#{debtAmount},#{status},#{approvalStatus},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmCustomer c);

    @Update("UPDATE crm_customer SET name=#{name},category_id=#{categoryId},linkman=#{linkman},phone=#{phone},address=#{address}," +
            "credit_limit=#{creditLimit},status=#{status},approval_status=#{approvalStatus},update_time=NOW() WHERE id=#{id}")
    int update(CrmCustomer c);

    @Update("UPDATE crm_customer SET approval_status=#{approvalStatus},update_time=NOW() WHERE id=#{id}")
    int updateApprovalStatus(@Param("id") Long id, @Param("approvalStatus") String approvalStatus);

    @Update("UPDATE crm_customer SET used_credit=used_credit+#{delta},debt_amount=debt_amount+#{delta},update_time=NOW() WHERE id=#{id}")
    int changeUsedCredit(@Param("id") Long id, @Param("delta") BigDecimal delta);

    @Update("UPDATE crm_customer SET used_credit=0,debt_amount=0,update_time=NOW() WHERE id=#{id}")
    int resetUsedCredit(@Param("id") Long id);

    @Update("UPDATE crm_customer SET credit_limit=#{creditLimit},update_time=NOW() WHERE id=#{id}")
    int updateCreditLimit(@Param("id") Long id, @Param("creditLimit") BigDecimal creditLimit);

    @Update("UPDATE crm_customer SET status='停用',merge_from=#{toId},update_time=NOW() WHERE id=#{fromId}")
    int disableForMerge(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Update("UPDATE crm_customer SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM crm_customer WHERE category_id=#{categoryId} AND deleted=0")
    int countByCategory(@Param("categoryId") Long categoryId);
}
