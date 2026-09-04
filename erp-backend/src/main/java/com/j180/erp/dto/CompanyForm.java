package com.j180.erp.dto;

import lombok.Data;

/**
 * 公司信息新增/编辑表单
 */
@Data
public class CompanyForm {

    private Long id;

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
