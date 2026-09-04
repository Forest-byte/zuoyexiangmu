package com.j180.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.j180.erp.entity.UserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-角色关联 Mapper
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
