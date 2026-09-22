package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户管理列表项（GET /api/users，管理员视图；较 UserInfoVO 多 status 字段）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserVO(
        /** 用户 id。 */
        Long id,
        /** 登录名。 */
        String username,
        /** 真实姓名。 */
        String realName,
        /** 部门 id。 */
        Long departmentId,
        /** 角色 id。 */
        Long roleId,
        /** 角色编码。 */
        String roleCode,
        /** 角色名称。 */
        String roleName,
        /** 账号状态：1 正常 / 0 停用。 */
        Integer status,
        /** 邮箱。 */
        String email,
        /** 手机号。 */
        String phone
) {
}
