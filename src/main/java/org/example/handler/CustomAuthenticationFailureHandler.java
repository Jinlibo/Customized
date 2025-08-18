package org.example.handler;

import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        //获取错误信息
        String localizedMessage = exception.getLocalizedMessage();

        //创建结果对象
        CommonResponse<String> commonResponse = CommonResponse.<String>builder().code(401).msg("登录失败").build();

        ResponseUtils.toJsonResponse(request, response, commonResponse);
    }
}