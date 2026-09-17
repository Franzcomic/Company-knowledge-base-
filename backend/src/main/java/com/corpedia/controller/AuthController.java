package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.LoginRequest;
import com.corpedia.dto.response.LoginResultVO;
import com.corpedia.dto.response.UserInfoVO;
import com.corpedia.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResultVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req.username(), req.password()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // P0 阶段无服务端黑名单；返回成功即可，前端清理 token
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.ok(authService.me());
    }
}