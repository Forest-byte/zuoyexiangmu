package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工-仓库关联表 sys_employee_warehouse（数据权限-本仓库维度）
 */
@Data
@TableName("sys_employee_warehouse")
public class EmployeeWarehouse implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long employeeId;

    private Long warehouseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
