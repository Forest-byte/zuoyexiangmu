package com.j180.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.j180.erp.entity.RoleResource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-资源关联 Mapper
 */
public interface RoleResourceMapper extends BaseMapper<RoleResource> {

    /**
     * 查询角色已授权的资源ID集合
     */
    @Select("SELECT resource_id FROM sys_role_resource WHERE role_id = #{roleId}")
    List<Long> selectResourceIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计角色已配置的资源数
     */
    @Select("SELECT COUNT(*) FROM sys_role_resource WHERE role_id = #{roleId}")
    long countByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计引用指定资源的角色数（资源删除限制校验）
     */
    @Select("SELECT COUNT(DISTINCT role_id) FROM sys_role_resource WHERE resource_id = #{resourceId}")
    long countRolesByResourceId(@Param("resourceId") Long resourceId);
}
