package com.erp.mapper;

import com.erp.entity.CrmSupplier;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SupplierMapper {

    @Select("<script>SELECT s.*, cat.name AS categoryName FROM crm_supplier s LEFT JOIN crm_category cat ON s.category_id=cat.id " +
            "WHERE s.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (s.name LIKE CONCAT('%',#{keyword},'%') OR s.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY s.id LIMIT #{offset},#{pageSize}</script>")
    List<CrmSupplier> page(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM crm_supplier s WHERE s.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (s.name LIKE CONCAT('%',#{keyword},'%') OR s.code LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("keyword") String keyword);

    @Select("SELECT * FROM crm_supplier WHERE deleted=0 ORDER BY id")
    List<CrmSupplier> selectAll();

    @Select("SELECT * FROM crm_supplier WHERE id=#{id} AND deleted=0")
    CrmSupplier findById(@Param("id") Long id);

    @Select("SELECT MAX(id) FROM crm_supplier WHERE deleted=0")
    Long maxId();

    @Insert("INSERT INTO crm_supplier(code,name,category_id,linkman,phone,address,payable_amount,status,create_time,create_by) " +
            "VALUES(#{code},#{name},#{categoryId},#{linkman},#{phone},#{address},#{payableAmount},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmSupplier s);

    @Update("UPDATE crm_supplier SET name=#{name},category_id=#{categoryId},linkman=#{linkman},phone=#{phone},address=#{address},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(CrmSupplier s);

    @Update("UPDATE crm_supplier SET payable_amount=payable_amount+#{delta},update_time=NOW() WHERE id=#{id}")
    int changePayable(@Param("id") Long id, @Param("delta") BigDecimal delta);

    @Update("UPDATE crm_supplier SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM crm_supplier WHERE category_id=#{categoryId} AND deleted=0")
    int countByCategory(@Param("categoryId") Long categoryId);
}
