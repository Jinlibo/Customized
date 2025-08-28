package org.example.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.RedisKeyConstant;
import org.example.entity.SysUser;
import org.example.security.CustomSecurityUser;
import org.example.util.JwtTokenUtil;
import org.example.util.ResponseUtils;
import org.example.vo.CommonResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器 - 优化版本
 */
@Component
@Slf4j
public class JwtCheckFilter extends OncePerRequestFilter {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 无需认证的路径
    private static final String[] EXCLUDE_PATHS = {
            "/auth/login",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/**",
            "/webjars/**",
            "/favicon.ico"
    };
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. 检查是否为排除路径
        if (isExcludePath(requestURI)) {
            doFilter(request, response, filterChain);
            return;
        }

        // 2. 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            responseAuthError(response, "Authorization header缺失");
            return;
        }

        // 3. 提取JWT Token
        String jwtToken;
        if (authHeader.toLowerCase().startsWith("bearer ")) {
            jwtToken = authHeader.substring(7).trim();
        } else {
            responseAuthError(response, "Authorization信息格式有误");
            return;
        }

        if (!StringUtils.hasText(jwtToken)) {
            responseAuthError(response, "token为空");
            return;
        }

        // 4. 验证JWT
        if (!JwtTokenUtil.checkJWT(jwtToken)) {
            responseAuthError(response, "token非法或已过期");
            return;
        }

        // 5. 从JWT中获取用户ID
        String userId = JwtTokenUtil.getUserId(jwtToken);
        if (!StringUtils.hasText(userId)) {
            responseAuthError(response, "token无效");
            return;
        }

        // 6. 从Redis获取用户会话
        String redisKey = RedisKeyConstant.USER_INFO_KEY + userId;
        Map<Object, Object> userSession = redisTemplate.opsForHash().entries(redisKey);
        if (userSession.isEmpty()) {
            responseAuthError(response, "用户会话已过期，请重新登录");
            return;
        }

        try {
            // 7. 验证Token一致性
            String sessionToken = (String) userSession.get("token");
            if (!jwtToken.equals(sessionToken)) {
                responseAuthError(response, "Token不匹配，可能在其他地方登录");
                return;
            }

            // 8. 获取用户信息和权限
            SysUser userInfo = new JSONObject((LinkedHashMap<String, Object>) userSession.get("user_info")).to(SysUser.class);
            List<String> authInfoList = JSON.parseArray(
                    JSON.toJSONString(userSession.get("auth_info")), String.class);

            if (userInfo == null || authInfoList == null) {
                responseAuthError(response, "用户会话数据异常");
                return;
            }

            // 9. 延长会话过期时间
            redisTemplate.expire(redisKey, 2, TimeUnit.HOURS);

            // 10. 构建Spring Security认证信息
            List<SimpleGrantedAuthority> authorities = authInfoList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            CustomSecurityUser customSecurityUser = new CustomSecurityUser(userInfo);
            customSecurityUser.setAuthorityList(authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customSecurityUser, null, authorities);

            // 11. 设置到Security上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("用户认证成功 - 用户ID: {}, 请求URI: {}", userId, requestURI);

        } catch (Exception e) {
            log.error("处理用户会话时出错 - 用户ID: {}, 错误: {}", userId, e.getMessage());
            responseAuthError(response, "用户会话处理异常");
            return;
        }

        // 12. 继续过滤链
        doFilter(request, response, filterChain);
    }

    /**
     * 检查是否为排除路径
     */
    private boolean isExcludePath(String requestURI) {
        return Arrays.stream(EXCLUDE_PATHS)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    /**
     * 返回认证错误响应
     */
    private void responseAuthError(HttpServletResponse response, String message) throws IOException {
        CommonResponse<String> responseMsg = CommonResponse.error(401, message);
        ResponseUtils.toJsonResponse(response, responseMsg);
    }
}