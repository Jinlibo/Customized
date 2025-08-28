package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.constant.RedisKeyConstant;
import org.example.entity.SysUser;
import org.example.entity.dto.LoginRequest;
import org.example.entity.dto.LoginResponse;
import org.example.security.CustomSecurityUser;
import org.example.util.JwtTokenUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务类 - 统一处理所有认证相关逻辑
 */
@Service
@Slf4j
public class AuthService {

    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录 - 唯一的登录方法
     */
    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        // 参数验证
        if (!StringUtils.hasText(loginRequest.getUsername()) ||
                !StringUtils.hasText(loginRequest.getPassword())) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }

        try {
            // 1. 创建认证token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername().trim(), loginRequest.getPassword());

            // 2. 执行认证
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 3. 认证成功，获取用户信息
            CustomSecurityUser securityUser = (CustomSecurityUser) authentication.getPrincipal();
            SysUser sysUser = securityUser.getSysUser();

            // 4. 生成JWT
            String jwtToken = JwtTokenUtil.createToken(String.valueOf(sysUser.getUserId()));

            // 5. 获取权限信息
            List<String> authList = securityUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // 6. 存储用户会话信息到redis
            String redisKey = RedisKeyConstant.USER_INFO_KEY + sysUser.getUserId();

            Map<String, Object> userSession = new HashMap<>();
            userSession.put("token", jwtToken);
            userSession.put("user_info", sysUser);
            userSession.put("auth_info", authList);
            LocalDateTime now = LocalDateTime.now();
            userSession.put("login_time", now);

            redisTemplate.opsForHash().putAll(redisKey, userSession);
            redisTemplate.expire(redisKey, 2, TimeUnit.HOURS);

            log.info("用户登录成功 - 用户ID: {}, 用户名: {}", sysUser.getUserId(), sysUser.getUsername());

            // 8. 构建响应
            return LoginResponse.builder()
                    .token(jwtToken)
                    .username(sysUser.getUsername())
                    .userId(sysUser.getUserId())
                    .loginTime(now)
                    .build();

        } catch (AuthenticationException e) {
            log.warn("用户认证失败 - 用户名: {}, 原因: {}", loginRequest.getUsername(), e.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        }
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
            Boolean deleted = redisTemplate.delete(redisKey);

            // 清除Security上下文
            SecurityContextHolder.clearContext();

            log.info("用户退出登录成功 - 用户ID: {}, 会话删除: {}", userId, deleted);
        } else {
            log.warn("退出登录失败：未找到当前登录用户");
            throw new RuntimeException("未找到当前登录用户");
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