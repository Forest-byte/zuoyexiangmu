package com.erp.mapper;

import com.erp.entity.FinConList;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FinConListMapper {

    @Select("<script>SELECT l.*, " +
            "CASE WHEN l.list_type='RECEIPT' THEN c.name ELSE s.name END AS partnerName, " +
            "a.name AS accountName FROM fin_con_list l " +
            "LEFT JOIN crm_customer c ON l.list_type='RECEIPT' AND l.partner_id=c.id " +
            "LEFT JOIN crm_supplier s ON l.list_type='PAYMENT' AND l.partner_id=s.id " +
            "LEFT JOIN fin_account a ON l.account_id=a.id " +
            "WHERE l.deleted=0 " +
            "<if test='listType!=null and listType!=\"\"'> AND l.list_type=#{listType}</if>" +
            "<if test='keyword!=null and keyword!=\"\"'> AND (l.list_no LIKE CONCAT('%',#{keyword},'%') OR l.orders_key LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY l.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<FinConList> page(@Param("listType") String listType, @Param("keyword") String keyword,
                          @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM fin_con_list l WHERE l.deleted=0 " +
            "<if test='listType!=null and listType!=\"\"'> AND l.list_type=#{listType}</if>" +
            "<if test='keyword!=null and keyword!=\"\"'> AND (l.list_no LIKE CONCAT('%',#{keyword},'%') OR l.orders_key LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("listType") String listType, @Param("keyword") String keyword);

    @Select("SELECT * FROM fin_con_list WHERE id=#{id} AND deleted=0")
    FinConList findById(@Param("id") Long id);

    @Insert("INSERT INTO fin_con_list(list_no,list_type,orders_key,partner_id,account_id,all_money,pay_type,receipt_date,states,payer,order_amount,is_dingdao,remark,create_time,create_by) " +
            "VALUES(#{listNo},#{listType},#{ordersKey},#{partnerId},#{accountId},#{allMoney},#{payType},#{receiptDate},#{states},#{payer},#{orderAmount},#{isDingdao},#{remark},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FinConList l);

    @Update("UPDATE fin_con_list SET states=#{states},is_dingdao=#{isDingdao},update_time=NOW() WHERE id=#{id}")
    int updateStates(@Param("id") Long id, @Param("states") String states, @Param("isDingdao") String isDingdao);

    @Update("UPDATE fin_con_list SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
