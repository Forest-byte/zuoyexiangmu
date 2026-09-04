package com.j180.erp.validator;

import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.CompanyForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 公司信息校验器
 */
@Component
public class CompanyValidator {

    /** 统一社会信用代码：18位 数字+大写字母 */
    private static final String CREDIT_CODE_PATTERN = "^[0-9A-Z]{18}$";
    /** 电话：数字、-()、空格、+，5-20位 */
    private static final String PHONE_PATTERN = "^[0-9\\-()\\s+]{5,20}$";
    private static final String EMAIL_PATTERN = "^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";

    public void validate(CompanyForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getCompanyCode(), 32, "公司编码不能为空");
        AssertUtil.text(form.getCompanyName(), 128, "公司名称不能为空");
        AssertUtil.text(form.getCreditCode(), 18, "统一社会信用代码不能为空");
        AssertUtil.isTrue(form.getCreditCode().matches(CREDIT_CODE_PATTERN), "统一社会信用代码格式不正确（18位数字或大写字母）");
        AssertUtil.matches(form.getPhone(), PHONE_PATTERN, "联系电话格式不正确");
        AssertUtil.matches(form.getEmail(), EMAIL_PATTERN, "邮箱格式不正确");
        AssertUtil.text(form.getLegalPerson(), 64, "法定代表人格式不正确");
        AssertUtil.maxLength(form.getAddress(), 256, "注册地址不能超过256个字符");
        AssertUtil.maxLength(form.getBankName(), 128, "开户银行不能超过128个字符");
        AssertUtil.maxLength(form.getBankAccount(), 64, "银行账号不能超过64个字符");
        AssertUtil.maxLength(form.getRemark(), 512, "备注不能超过512个字符");
        if (StringUtils.hasText(form.getBankAccount())) {
            AssertUtil.matches(form.getBankAccount(), "^\\d{8,32}$", "银行账号格式不正确");
        }
        if (form.getStatus() != null) {
            StatusEnum.check(form.getStatus());
        }
    }
}
