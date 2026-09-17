package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户管理列表项（GET /api/users，管理员视图；较 UserInfoVO 多 status 字段）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserVO(
        Long id,
        String username,
        String realName,
        Long departmentId,
        Long roleId,
        String roleCode,
        String roleName,
        Integer status,
        String email,
        String phone
) {
}
