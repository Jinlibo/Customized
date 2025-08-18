package org.example.handler;

import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        //请求未授权的接口
        System.out.println(accessDeniedException);
        //创建结果对象
        CommonResponse<String> commonResponse = CommonResponse.<String>builder().code(401).msg("没有权限").build();
        ResponseUtils.toJsonResponse(request, response, commonResponse);
    }
}
