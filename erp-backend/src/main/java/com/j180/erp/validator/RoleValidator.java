package com.j180.erp.validator;

import com.j180.erp.common.enums.DataScopeEnum;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.RoleForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 角色信息校验器
 */
@Component
public class RoleValidator {

    public void validate(RoleForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getRoleCode(), 32, "角色编码不能为空");
        AssertUtil.text(form.getRoleName(), 64, "角色名称不能为空");
        AssertUtil.maxLength(form.getDescription(), 256, "角色描述不能超过256个字符");
        AssertUtil.maxLength(form.getRemark(), 512, "备注不能超过512个字符");
        if (form.getDataScope() != null) {
            DataScopeEnum.of(form.getDataScope());
            validateScopeIds(form.getDataScope(), form.getDataScopeIds());
        }
        if (form.getStatus() != null) {
            StatusEnum.check(form.getStatus());
        }
    }

    /**
     * 数据权限设置专用校验（供角色授权服务复用）
     */
    public void validateDataScope(Integer dataScope, String dataScopeIds) {
        AssertUtil.notNull(dataScope, "数据权限范围不能为空");
        DataScopeEnum.of(dataScope);
        validateScopeIds(dataScope, dataScopeIds);
    }

    /**
     * 数据权限明细校验：1=全部 4=本人 不允许填明细；2/3/5 允许填逗号分隔的正整数ID
     */
    private void validateScopeIds(Integer dataScope, String dataScopeIds) {
        if (dataScope == DataScopeEnum.ALL.getCode() || dataScope == DataScopeEnum.SELF.getCode()) {
            AssertUtil.isFalse(StringUtils.hasText(dataScopeIds), "该数据范围不允许配置明细ID");
            return;
        }
        if (!StringUtils.hasText(dataScopeIds)) {
            return;
        }
        for (String s : dataScopeIds.split(",")) {
            if (StringUtils.hasText(s)) {
                AssertUtil.isTrue(s.trim().matches("\\d+"), "数据范围明细ID格式不正确");
            }
        }
    }
}
