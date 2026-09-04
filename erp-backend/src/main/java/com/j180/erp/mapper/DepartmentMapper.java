package com.j180.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.j180.erp.entity.Department;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门信息 Mapper
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 查询部门及其全部子孙部门ID（ancestors 含自身，故结果含 deptId 本身）
     */
    @Select("SELECT id FROM sys_department WHERE FIND_IN_SET(#{deptId}, ancestors)")
    List<Long> selectSubtreeIds(@Param("deptId") Long deptId);
}
