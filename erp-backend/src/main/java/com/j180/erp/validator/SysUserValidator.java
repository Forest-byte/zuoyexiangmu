package com.j180.erp.validator;

import com.j180.erp.common.Constants;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.UserForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户账号校验器
 */
@Component
public class SysUserValidator {

    public void validate(UserForm form, boolean isCreate) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getUsername(), 64, "登录名不能为空");
        AssertUtil.isTrue(form.getUsername().matches("^[a-zA-Z0-9_]{2,32}$"), "登录名仅允许字母、数字、下划线，长度2-32位");
        if (isCreate) {
            AssertUtil.text(form.getPassword(), 32, "初始密码不能为空");
            AssertUtil.matches(form.getPassword(), Constants.PASSWORD_PATTERN, Constants.PASSWORD_TIP);
        } else if (StringUtils.hasText(form.getPassword())) {
            AssertUtil.matches(form.getPassword(), Constants.PASSWORD_PATTERN, Constants.PASSWORD_TIP);
        }
        if (form.getStatus() != null) {
            StatusEnum.check(form.getStatus());
        }
    }

    /** 校验重置/修改密码 */
    public void validatePassword(String newPassword) {
        AssertUtil.text(newPassword, 32, "新密码不能为空");
        AssertUtil.matches(newPassword, Constants.PASSWORD_PATTERN, Constants.PASSWORD_TIP);
    }
}
