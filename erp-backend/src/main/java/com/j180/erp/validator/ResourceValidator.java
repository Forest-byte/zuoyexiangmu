package com.j180.erp.validator;

import com.j180.erp.common.enums.ResourceTypeEnum;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.ResourceForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 资源信息校验器（层级约束依赖父资源类型，在 Service 层结合数据库校验）
 */
@Component
public class ResourceValidator {

    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE");

    public void validate(ResourceForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.text(form.getResCode(), 64, "资源编码不能为空");
        AssertUtil.text(form.getResName(), 64, "资源名称不能为空");
        AssertUtil.notNull(form.getResType(), "资源类型不能为空");
        ResourceTypeEnum.of(form.getResType());
        AssertUtil.notNull(form.getParentId(), "上级资源不能为空");
        AssertUtil.isTrue(form.getParentId() >= 0, "上级资源不能为负数");
        AssertUtil.maxLength(form.getPath(), 256, "资源路径不能超过256个字符");
        AssertUtil.maxLength(form.getIcon(), 64, "图标不能超过64个字符");
        if (form.getResType() == ResourceTypeEnum.API.getCode()) {
            AssertUtil.text(form.getPath(), 256, "接口资源路径不能为空");
            AssertUtil.notBlank(form.getHttpMethod(), "接口资源请求方法不能为空");
            AssertUtil.isTrue(HTTP_METHODS.contains(form.getHttpMethod().toUpperCase()), "请求方法仅允许 GET/POST/PUT/DELETE");
        } else if (StringUtils.hasText(form.getHttpMethod())) {
            AssertUtil.isTrue(HTTP_METHODS.contains(form.getHttpMethod().toUpperCase()), "请求方法仅允许 GET/POST/PUT/DELETE");
        }
        if (form.getStatus() != null) {
            StatusEnum.check(form.getStatus());
        }
    }
}
