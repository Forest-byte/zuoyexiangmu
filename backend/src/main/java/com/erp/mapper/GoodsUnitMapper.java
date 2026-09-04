package com.erp.mapper;

import com.erp.entity.GoodsUnit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GoodsUnitMapper {

    @Select("SELECT * FROM goods_unit WHERE deleted=0 ORDER BY id")
    List<GoodsUnit> selectAll();

    @Insert("INSERT INTO goods_unit(name,rate,create_time,create_by) VALUES(#{name},#{rate},NOW(),#{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GoodsUnit u);

    @Update("UPDATE goods_unit SET name=#{name},rate=#{rate},update_time=NOW() WHERE id=#{id}")
    int update(GoodsUnit u);

    @Update("UPDATE goods_unit SET deleted=1,update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM goods WHERE unit_id=#{id} AND deleted=0")
    int countGoodsRef(@Param("id") Long id);
}
