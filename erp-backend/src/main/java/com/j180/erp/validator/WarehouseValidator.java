package com.j180.erp.validator;

import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.WarehouseForm;
import org.springframework.stereotype.Component;

/**
 * 仓库信息校验器
 */
@Component
public class WarehouseValidator {

    private static final String PHONE_PATTERN = "^[0-9\\-()\\s+]{5,20}$";

    public void validate(WarehouseForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getWhCode(), 32, "仓库编码不能为空");
        AssertUtil.text(form.getWhName(), 64, "仓库名称不能为空");
        AssertUtil.notNull(form.getWhType(), "仓库类型不能为空");
        AssertUtil.isTrue(form.getWhType() >= 1 && form.getWhType() <= 5, "仓库类型非法，仅允许1-5");
        AssertUtil.matches(form.getPhone(), PHONE_PATTERN, "联系电话格式不正确");
        AssertUtil.maxLength(form.getRegion(), 128, "所在地区不能超过128个字符");
        AssertUtil.maxLength(form.getAddress(), 256, "详细地址不能超过256个字符");
        AssertUtil.maxLength(form.getContact(), 64, "联系人不能超过64个字符");
        AssertUtil.maxLength(form.getRemark(), 512, "备注不能超过512个字符");
        if (form.getStatus() != null) {
            StatusEnum.check(form.getStatus());
        }
    }
}
