package com.j180.erp.dto;

import com.j180.erp.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户信息（含权限），登录 / me 接口返回
 */
@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private Long employeeId;

    private String employeeName;

    /** 是否超级管理员 */
    private boolean superAdmin;

    /** 数据权限范围编码 */
    private Integer dataScope;

    /** 拥有的功能权限资源编码集合（null=全部） */
    private List<String> permissions;

    /** 可见菜单树（菜单+页面） */
    private List<ResourceTreeNode> menus;

    private List<Long> roleIds;

    private List<String> roleNames;

    private LocalDateTime lastLoginTime;

    public static UserInfoVO from(SysUser user, String employeeName) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setEmployeeName(employeeName);
        vo.setLastLoginTime(user.getLastLoginTime());
        return vo;
    }
}
