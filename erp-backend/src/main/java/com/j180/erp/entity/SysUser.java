package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录账号表 sys_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    /** 密码哈希，任何接口不回显 */
    @JsonIgnore
    private String passwordHash;

    private Long employeeId;

    /** 0=停用 1=启用 */
    private Integer status;

    /** 1=内置账号（不可删除/停用/移出内置角色） */
    private Integer isBuiltin;

    private LocalDateTime lastLoginTime;
}
