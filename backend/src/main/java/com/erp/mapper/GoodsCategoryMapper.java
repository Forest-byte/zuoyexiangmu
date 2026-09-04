package com.erp.mapper;

import com.erp.entity.GoodsCategory;
import com.erp.entity.GoodsUnit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GoodsCategoryMapper {

    @Select("SELECT * FROM goods_category WHERE deleted=0 ORDER BY id")
    List<GoodsCategory> selectAll();

    @Insert("INSERT INTO goods_category(parent_id,name,create_time,create_by) VALUES(#{parentId},#{name},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GoodsCategory c);

    @Update("UPDATE goods_category SET parent_id=#{parentId},name=#{name},update_time=NOW() WHERE id=#{id}")
    int update(GoodsCategory c);

    @Update("UPDATE goods_category SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM goods_category WHERE parent_id=#{id} AND deleted=0")
    int countChildren(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM goods WHERE category_id=#{id} AND deleted=0")
    int countGoodsRef(@Param("id") Long id);
}
