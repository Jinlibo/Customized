package org.example.handler;

import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        //获取错误信息
        //String localizedMessage = authException.getLocalizedMessage();

        //创建结果对象
        CommonResponse<String> commonResponse = CommonResponse.<String>builder().code(401).msg("需要登录").build();

        ResponseUtils.toJsonResponse(response, commonResponse);
    }
}