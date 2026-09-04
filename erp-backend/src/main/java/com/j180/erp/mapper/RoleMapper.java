package com.j180.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.j180.erp.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询用户的全部启用角色
     */
    @Select("SELECT r.* FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<Role> selectEnabledByUserId(@Param("userId") Long userId);

    /**
     * 查询角色下的全部用户ID
     */
    @Select("SELECT ur.user_id FROM sys_user_role ur WHERE ur.role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计角色下的用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user_role ur WHERE ur.role_id = #{roleId}")
    long countUsersByRoleId(@Param("roleId") Long roleId);
}
