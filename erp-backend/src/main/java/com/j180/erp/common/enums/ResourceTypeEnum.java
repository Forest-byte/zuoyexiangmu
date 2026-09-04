package com.j180.erp.common.enums;

import com.j180.erp.common.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 资源类型枚举（含层级约束：每种父类型允许挂载的子类型）
 */
@Getter
@AllArgsConstructor
public enum ResourceTypeEnum {

    MENU(1, "菜单", Set.of()),
    PAGE(2, "页面", Set.of(1)),
    BUTTON(3, "按钮", Set.of(1, 2)),
    API(4, "接口", Set.of(1, 2));

    private final int code;
    private final String label;
    /** 允许作为上级的类型编码集合 */
    private final Set<Integer> allowedParentTypes;

    public static ResourceTypeEnum of(Integer code) {
        for (ResourceTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new BizException("非法的资源类型: " + code);
    }

    /**
     * 层级约束校验：parentId=0 顶级仅允许菜单；BUTTON/API 不可再挂子资源
     */
    public static void checkHierarchy(Integer parentType, Integer childType) {
        ResourceTypeEnum child = of(childType);
        if (parentType == null || parentType == 0) {
            if (child != MENU) {
                throw new BizException("顶级资源仅允许为菜单类型");
            }
            return;
        }
        ResourceTypeEnum parent = of(parentType);
        AssertUtilPass.assertTrue(parent.allowedParentTypes.contains(child.code),
                "层级约束不合法：" + parent.label + "下不可挂载" + child.label + "类型资源");
    }

    /** 内部断言透传（避免循环依赖直接引用断言工具语义） */
    private static class AssertUtilPass {
        static void assertTrue(boolean expression, String message) {
            if (!expression) {
                throw new BizException(message);
            }
        }
    }
}
