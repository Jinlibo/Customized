package org.example.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "登录请求")
@GroupSequence({LoginRequest.captcha.class, LoginRequest.class})
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空", groups = LoginRequest.captcha.class)
    private String captcha;

    public interface captcha {
    }
}