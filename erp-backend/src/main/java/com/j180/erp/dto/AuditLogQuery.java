package com.j180.erp.dto;

import com.j180.erp.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审计日志分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogQuery extends PageQuery {

    /** 关键字（模块/操作类型/操作人/对象类型模糊） */
    private String keyword;

    /** 所属模块精确匹配 */
    private String module;

    /** 操作类型精确匹配 */
    private String actionType;

    /** 操作人登录名 */
    private String username;

    /** 操作时间起 */
    private LocalDateTime startTime;

    /** 操作时间止 */
    private LocalDateTime endTime;
}
