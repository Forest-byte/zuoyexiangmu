package com.j180.erp.validator;

import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.DepartmentForm;
import org.springframework.stereotype.Component;

/**
 * 部门信息校验器
 */
@Component
public class DepartmentValidator {

    public void validate(DepartmentForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getDeptName(), 32, "部门名称不能为空");
        AssertUtil.text(form.getDeptCode(), 32, "部门编码不能为空");
        AssertUtil.notNull(form.getParentId(), "上级部门不能为空");
        AssertUtil.isTrue(form.getParentId() >= 0, "上级部门不能为负数");
        if (form.getSort() != null) {
            AssertUtil.isTrue(form.getSort() >= 0 && form.getSort() <= 9999, "排序号必须在0-9999之间");
        }
        if (form.getStatus() != null) {
            StatusEnum.check(form.getStatus());
        }
    }
}
