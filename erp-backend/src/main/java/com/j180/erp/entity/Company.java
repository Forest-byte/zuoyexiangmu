package com.j180.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公司信息表 sys_company
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_company")
public class Company extends BaseEntity {

    private String companyCode;

    private String companyName;

    private String creditCode;

    private String legalPerson;

    private String address;

    private String phone;

    private String email;

    private String bankName;

    private String bankAccount;

    private Integer status;

    private String remark;
}
