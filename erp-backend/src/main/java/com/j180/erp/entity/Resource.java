package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源表 sys_resource（1=菜单 2=页面 3=按钮 4=接口）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_resource")
public class Resource extends BaseEntity {

    private String resCode;

    private String resName;

    private Integer resType;

    private Long parentId;

    private String path;

    private String httpMethod;

    private Integer sortNo;

    private String icon;

    private Integer status;
}
