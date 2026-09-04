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
 * 角色-资源关联表 sys_role_resource
 */
@Data
@TableName("sys_role_resource")
public class RoleResource implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private Long resourceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
