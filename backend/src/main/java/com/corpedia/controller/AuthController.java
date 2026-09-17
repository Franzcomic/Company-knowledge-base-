package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.LoginRequest;
import com.corpedia.dto.response.LoginResultVO;
import com.corpedia.dto.response.UserInfoVO;
import com.corpedia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "登录 / 登出 / 当前用户")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登录", description = "账号密码登录，返回 JWT token 与用户信息。演示账号仅在启用种子数据后创建，密码由 SEED_PASSWORD 配置。")
    @SecurityRequirements
    @PostMapping("/login")
    public Result<LoginResultVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req.username(), req.password()));
    }

    @Operation(summary = "登出", description = "P0 无服务端黑名单，前端清理 token 即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // P0 阶段无服务端黑名单；返回成功即可，前端清理 token
        return Result.ok();
    }

    @Operation(summary = "当前用户信息", description = "返回当前登录用户信息（需 Bearer token）")
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.ok(authService.me());
    }
}
