package com.j180.erp.dto;

import com.j180.erp.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录账号视图
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private Integer status;

    private Integer isBuiltin;

    private LocalDateTime lastLoginTime;

    private Long employeeId;

    private String employeeName;

    private String departmentName;

    private List<Long> roleIds;

    private List<String> roleNames;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static UserVO from(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setStatus(user.getStatus());
        vo.setIsBuiltin(user.getIsBuiltin());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
