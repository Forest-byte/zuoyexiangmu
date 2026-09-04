package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库信息表 wms_warehouse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_warehouse")
public class Warehouse extends BaseEntity {

    private String whCode;

    private String whName;

    /** 1=原材料仓 2=成品仓 3=半成品仓 4=退货仓 5=其他 */
    private Integer whType;

    private Long managerId;

    private String region;

    private String address;

    private String contact;

    private String phone;

    private Integer status;

    private String remark;
}
