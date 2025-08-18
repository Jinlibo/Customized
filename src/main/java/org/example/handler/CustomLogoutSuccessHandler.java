package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.constant.RedisKeyConstant;
import org.example.security.CustomSecurityUser;
import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomSecurityUser securityUser = (CustomSecurityUser) authentication.getPrincipal();

        Integer userId = securityUser.getSysUser().getUserId();

        Boolean delete = redisTemplate.delete(RedisKeyConstant.USER_INFO_KEY + userId);

        log.info("用户退出成功，删除redis key{},缓存成功:{}", userId, delete);

        //创建结果对象
        CommonResponse<Object> commonResponse = CommonResponse.success(null);

        ResponseUtils.toJsonResponse(request, response, commonResponse);
    }
}