package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.LoginRequest;
import org.example.entity.dto.LoginResponse;
import org.example.service.impl.AuthService;
import org.example.vo.CommonResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
@Slf4j
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT token")
    public CommonResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return CommonResponse.success(loginResponse);
        } catch (Exception e) {
            log.error("登录失败", e);
            return CommonResponse.<LoginResponse>builder()
                    .code(401)
                    .msg("登录失败：" + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "退出登录，清除会话")
    public CommonResponse<String> logout() {
        try {
            authService.logout();
            return CommonResponse.success("退出成功");
        } catch (Exception e) {
            log.error("退出失败", e);
            return CommonResponse.<String>builder()
                    .code(500)
                    .msg("退出失败：" + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/userinfo")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息")
    public CommonResponse<Object> getCurrentUser() {
        try {
            Object userInfo = authService.getCurrentUserInfo();
            return CommonResponse.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return CommonResponse.builder()
                    .code(500)
                    .msg("获取用户信息失败：" + e.getMessage())
                    .build();
        }
    }
}