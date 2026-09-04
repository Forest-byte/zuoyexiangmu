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
 * 操作/权限审计日志表 sys_audit_log
 */
@Data
@TableName("sys_audit_log")
public class AuditLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String actionType;

    private String targetType;

    private Long targetId;

    private String beforeSnapshot;

    private String afterSnapshot;

    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
