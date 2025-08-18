package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.constant.RedisKeyConstant;
import org.example.entity.SysUser;
import org.example.security.CustomSecurityUser;
import org.example.util.JwtUtils;
import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //从认证信息中获取登录用户信息
        CustomSecurityUser securityUser = (CustomSecurityUser) authentication.getPrincipal();
        SysUser sysUser = securityUser.getSysUser();
        String strUserInfo = String.valueOf(sysUser.getUserId());
        //获取用户的权限信息
        List<SimpleGrantedAuthority> authorities = securityUser.getAuthorities().stream()
                .filter(authority -> authority instanceof SimpleGrantedAuthority)
                .map(authority -> (SimpleGrantedAuthority) authority)
                .collect(Collectors.toList());
        // collect 收集
        List<String> authList = authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList());
        //生成jwt
        String jwtToken = jwtUtils.createJwt(strUserInfo);
        HashMap<String, Object> userSession = new HashMap<>();
        userSession.put("token", jwtToken);
        userSession.put("userInfo", sysUser);
        userSession.put("authInfo", authList);
        redisTemplate.opsForHash().putAll(RedisKeyConstant.USER_INFO_KEY + sysUser.getUserId(), userSession);
        redisTemplate.expire(RedisKeyConstant.USER_INFO_KEY + sysUser.getUserId(), 2, TimeUnit.HOURS);
        CommonResponse<String> httpResult = CommonResponse.success(jwtToken);
        ResponseUtils.toJsonResponse(request, response, httpResult);
    }

}
