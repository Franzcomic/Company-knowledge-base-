package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.UserUpdateRequest;
import com.corpedia.dto.response.UserVO;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.PermissionService;
import com.corpedia.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理（阶段4 P1，仅 SYS_ADMIN）。
 */
@Tag(name = "用户管理", description = "用户列表 / 修改部门·角色·启停用（仅系统管理员）")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;

    public UserController(UserService userService, PermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @Operation(summary = "用户列表", description = "返回全部用户（含角色 code/name 与状态）")
    @GetMapping
    public Result<List<UserVO>> list() {
        permissionService.requireUserManage(currentCtx());
        return Result.ok(userService.list());
    }

    @Operation(summary = "修改用户", description = "改 departmentId/roleId/status，仅传需修改字段")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "用户 id") @PathVariable Long id,
                               @RequestBody UserUpdateRequest req) {
        permissionService.requireUserManage(currentCtx());
        userService.update(id, req);
        return Result.ok();
    }

    private UserContext currentCtx() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null) {
            throw permissionService.forbidden();
        }
        return ctx;
    }
}
