package com.erp.mapper;

import com.erp.entity.Goods;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GoodsMapper {

    @Select("<script>SELECT g.*, c.name AS categoryName, u.name AS unitName FROM goods g " +
            "LEFT JOIN goods_category c ON g.category_id=c.id LEFT JOIN goods_unit u ON g.unit_id=u.id " +
            "WHERE g.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (g.name LIKE CONCAT('%',#{keyword},'%') OR g.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='categoryId!=null'> AND g.category_id=#{categoryId}</if>" +
            "<if test='status!=null and status!=\"\"'> AND g.status=#{status}</if>" +
            " ORDER BY g.id LIMIT #{offset},#{pageSize}</script>")
    List<Goods> page(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                     @Param("status") String status, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM goods g WHERE g.deleted=0 " +
            "<if test='keyword!=null and keyword!=\"\"'> AND (g.name LIKE CONCAT('%',#{keyword},'%') OR g.code LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='categoryId!=null'> AND g.category_id=#{categoryId}</if>" +
            "<if test='status!=null and status!=\"\"'> AND g.status=#{status}</if></script>")
    long count(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("status") String status);

    @Select("SELECT g.*, c.name AS categoryName, u.name AS unitName FROM goods g " +
            "LEFT JOIN goods_category c ON g.category_id=c.id LEFT JOIN goods_unit u ON g.unit_id=u.id " +
            "WHERE g.deleted=0 ORDER BY g.id")
    List<Goods> selectAll();

    @Select("SELECT * FROM goods WHERE id=#{id} AND deleted=0")
    Goods findById(@Param("id") Long id);

    @Select("SELECT * FROM goods WHERE status='在售' AND deleted=0 ORDER BY id")
    List<Goods> selectOnSale();

    @Select("SELECT MAX(id) FROM goods WHERE deleted=0")
    Long maxId();

    @Insert("INSERT INTO goods(code,name,category_id,unit_id,spec,brand,barcode,purchase_price,sale_price,last_in_price,low_limit,high_limit,supplier_id,is_raw,status,create_time,create_by) " +
            "VALUES(#{code},#{name},#{categoryId},#{unitId},#{spec},#{brand},#{barcode},#{purchasePrice},#{salePrice},#{lastInPrice},#{lowLimit},#{highLimit},#{supplierId},#{isRaw},#{status},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Goods g);

    @Update("UPDATE goods SET code=#{code},name=#{name},category_id=#{categoryId},unit_id=#{unitId},spec=#{spec},brand=#{brand},barcode=#{barcode}," +
            "purchase_price=#{purchasePrice},sale_price=#{salePrice},last_in_price=#{lastInPrice},low_limit=#{lowLimit},high_limit=#{highLimit}," +
            "supplier_id=#{supplierId},is_raw=#{isRaw},status=#{status},update_time=NOW() WHERE id=#{id}")
    int update(Goods g);

    @Update("UPDATE goods SET low_limit=#{lowLimit},high_limit=#{highLimit},update_time=NOW() WHERE id=#{id}")
    int updateLimits(@Param("id") Long id, @Param("lowLimit") java.math.BigDecimal lowLimit,
                     @Param("highLimit") java.math.BigDecimal highLimit);

    @Update("UPDATE goods SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM goods WHERE code=#{code} AND deleted=0")
    int countByCode(@Param("code") String code);
}
