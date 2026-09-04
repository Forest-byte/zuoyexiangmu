package com.j180.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.j180.erp.entity.EmployeeWarehouse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工-仓库关联 Mapper
 */
public interface EmployeeWarehouseMapper extends BaseMapper<EmployeeWarehouse> {

    @Select("SELECT warehouse_id FROM sys_employee_warehouse WHERE employee_id = #{employeeId}")
    List<Long> selectWarehouseIdsByEmployeeId(@Param("employeeId") Long employeeId);

    @Select("SELECT COUNT(*) FROM sys_employee_warehouse WHERE warehouse_id = #{warehouseId}")
    long countByWarehouseId(@Param("warehouseId") Long warehouseId);
}
