package com.j180.erp.validator;

import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.EmployeeForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 员工信息校验器
 */
@Component
public class EmployeeValidator {

    /** 手机号：中国大陆11位 */
    private static final String MOBILE_PATTERN = "^1[3-9]\\d{9}$";
    private static final String EMAIL_PATTERN = "^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";
    /** 身份证：18位末位可为X */
    private static final String ID_CARD_PATTERN = "^\\d{17}[0-9Xx]$";

    public void validate(EmployeeForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getEmpNo(), 32, "员工编号不能为空");
        AssertUtil.text(form.getName(), 64, "姓名不能为空");
        AssertUtil.notNull(form.getHireDate(), "入职日期不能为空");
        AssertUtil.isTrue(form.getHireDate() != null && form.getHireDate().getYear() >= 1970,
                "入职日期非法");
        AssertUtil.matches(form.getMobile(), MOBILE_PATTERN, "手机号格式不正确");
        AssertUtil.matches(form.getEmail(), EMAIL_PATTERN, "邮箱格式不正确");
        if (StringUtils.hasText(form.getIdCard())) {
            AssertUtil.isTrue(form.getIdCard().matches(ID_CARD_PATTERN), "身份证号格式不正确（18位）");
        }
        if (form.getGender() != null) {
            AssertUtil.isTrue(form.getGender() == 0 || form.getGender() == 1, "性别非法，仅允许0=男 1=女");
        }
        if (form.getStatus() != null) {
            AssertUtil.isTrue(form.getStatus() >= 0 && form.getStatus() <= 2, "员工状态非法，仅允许0=试用 1=在职 2=离职");
        }
        AssertUtil.maxLength(form.getPosition(), 64, "岗位不能超过64个字符");
        AssertUtil.maxLength(form.getLevel(), 32, "职级不能超过32个字符");
        AssertUtil.maxLength(form.getRemark(), 512, "备注不能超过512个字符");
    }

    /** 校验离职操作相关约束 */
    public void validateLeave(Integer status) {
        AssertUtil.isTrue(status == null || status != 2, "员工已是离职状态");
    }
}
