package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.constant.RedisKeyConstant;
import org.example.entity.SysUser;
import org.example.entity.dto.LoginRequest;
import org.example.entity.dto.LoginResponse;
import org.example.security.CustomSecurityUser;
import org.example.util.JwtUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthService {

    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * REST风格登录
     */
    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        // 1. 创建认证token
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        // 2. 进行认证
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. 认证成功，获取用户信息
        CustomSecurityUser securityUser = (CustomSecurityUser) authentication.getPrincipal();
        SysUser sysUser = securityUser.getSysUser();

        // 4. 生成JWT
        String jwtToken = jwtUtils.createJwt(String.valueOf(sysUser.getUserId()));

        // 5. 获取权限信息
        List<String> authList = securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 6. 存储到Redis
        long currentTime = System.currentTimeMillis();
        HashMap<String, Object> userSession = new HashMap<>();
        userSession.put("token", jwtToken);
        userSession.put("user_info", sysUser);
        userSession.put("auth_info", authList);
        userSession.put("login_time", currentTime);

        String redisKey = RedisKeyConstant.USER_INFO_KEY + sysUser.getUserId();
        redisTemplate.opsForHash().putAll(redisKey, userSession);
        redisTemplate.expire(redisKey, 2, TimeUnit.HOURS);

        // 7. 构建响应
        return LoginResponse.builder()
                .token(jwtToken)
                .username(sysUser.getUsername())
                .userId(sysUser.getUserId())
                .loginTime(currentTime)
                .expireTime(currentTime + 2 * 60 * 60 * 1000) // 2小时后过期
                .build();
    }

    /**
     * 退出登录
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomSecurityUser) {
            CustomSecurityUser securityUser = (CustomSecurityUser) authentication.getPrincipal();
            Integer userId = securityUser.getSysUser().getUserId();

            // 删除Redis中的会话
            String redisKey = RedisKeyConstant.USER_INFO_KEY + userId;
            redisTemplate.delete(redisKey);

            // 清除Security上下文
            SecurityContextHolder.clearContext();

            log.info("用户 {} 退出登录成功", userId);
        }
    }

    /**
     * 获取当前用户信息
     */
    public Object getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomSecurityUser) {
            CustomSecurityUser securityUser = (CustomSecurityUser) authentication.getPrincipal();
            return securityUser.getSysUser();
        }
        throw new RuntimeException("未找到当前用户信息");
    }
}
