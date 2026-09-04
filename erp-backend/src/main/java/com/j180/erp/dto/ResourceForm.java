package com.j180.erp.dto;

import lombok.Data;

/**
 * 资源表单
 */
@Data
public class ResourceForm {

    private Long id;

    /** 资源编码（全局唯一） */
    private String resCode;

    /** 资源名称 */
    private String resName;

    /** 1=菜单 2=页面 3=按钮 4=接口 */
    private Integer resType;

    /** 上级资源ID(0=顶级) */
    private Long parentId;

    /** 资源路径/URL（菜单/页面/接口必填） */
    private String path;

    /** 接口方法（仅接口类型） */
    private String httpMethod;

    private Integer sortNo;

    private String icon;

    private Integer status;
}
