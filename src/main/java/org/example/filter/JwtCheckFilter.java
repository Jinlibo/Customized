package org.example.filter;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.RedisKeyConstant;
import org.example.entity.SysUser;
import org.example.security.CustomSecurityUser;
import org.example.util.JwtUtils;
import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtCheckFilter extends OncePerRequestFilter {
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        //如果是登录请求urI，直接放行
        if (requestURI.equals("/login")) {
            doFilter(request, response, filterChain);
            return;
        }
        String strAuth = request.getHeader("Authorization");
        if (!StringUtils.hasText(strAuth)) {
            CommonResponse<String> httpResult = CommonResponse.<String>builder()
                    .code(401)
                    .msg("Authorization 为空")
                    .build();
            ResponseUtils.toJsonResponse(request, response, httpResult);
            return;
        }
        String jwtToken = strAuth.replace("bearer ", "");
        if (StringUtils.containsWhitespace(jwtToken)) {
            CommonResponse<String> httpResult = CommonResponse.<String>builder()
                    .code(401)
                    .msg("jwt 为空")
                    .build();
            ResponseUtils.toJsonResponse(request, response, httpResult);
            return;
        }
        //校验jwt
        boolean verifyResult = jwtUtils.verifyToken(jwtToken);
        if (!verifyResult) {
            CommonResponse<String> httpResult = CommonResponse.<String>builder()
                    .code(0)
                    .msg("token不正确或已过期！")
                    .build();
            ResponseUtils.toJsonResponse(request, response, httpResult);
            return;
        }
        //从jwt里获取用户id
        String userId = jwtUtils.getUserInfoFromToken(jwtToken);
        //redis获取用户session
        Map<Object, Object> userSession = redisTemplate.opsForHash().entries(RedisKeyConstant.USER_INFO_KEY + userId);
        if (userSession.isEmpty()) {
            CommonResponse<String> commonResponse = CommonResponse.<String>builder()
                    .code(0)
                    .msg("用户未登录！")
                    .build();
            ResponseUtils.toJsonResponse(request, response, commonResponse);
            return;
        }
        List<String> authInfo = JSON.parseArray(JSON.toJSONString(userSession.get("auth_info")), String.class);
        SysUser userInfo = (SysUser) userSession.get("user_info");
        String token = (String) userSession.get("token");
        if (!token.equals(jwtToken)) {
            CommonResponse<String> commonResponse = CommonResponse.<String>builder().code(401).msg("token非法！").build();
            ResponseUtils.toJsonResponse(request, response, commonResponse);
            return;
        }
        redisTemplate.expire(RedisKeyConstant.USER_INFO_KEY + userId, 2, TimeUnit.HOURS);
        List<SimpleGrantedAuthority> authorityList = authInfo.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        CustomSecurityUser mySecurityUser = new CustomSecurityUser(userInfo);
        //用户名密码认证token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mySecurityUser, null, authorityList);
        //把token放到安全上下文：securityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        doFilter(request, response, filterChain); //放行
    }
}
