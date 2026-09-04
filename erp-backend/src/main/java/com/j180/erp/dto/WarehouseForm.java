package com.j180.erp.dto;

import lombok.Data;

/**
 * 仓库表单
 */
@Data
public class WarehouseForm {

    private Long id;

    /** 仓库编码（唯一） */
    private String whCode;

    /** 仓库名称（唯一） */
    private String whName;

    /** 1=原材料仓 2=成品仓 3=半成品仓 4=退货仓 5=其他 */
    private Integer whType;

    /** 负责人（仅可选在职员工） */
    private Long managerId;

    private String region;

    private String address;

    private String contact;

    private String phone;

    private Integer status;

    private String remark;
}
