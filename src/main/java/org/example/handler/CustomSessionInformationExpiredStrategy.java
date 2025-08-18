package org.example.handler;

import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;

public class CustomSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {

        //创建结果对象
        CommonResponse<String> commonResponse = CommonResponse.<String>builder().code(401).msg("该账号已从其他设备登录").build();

        ResponseUtils.toJsonResponse(event.getRequest(), event.getResponse(), commonResponse);
    }
}