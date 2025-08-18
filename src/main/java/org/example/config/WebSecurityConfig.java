package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.filter.JwtCheckFilter;
import org.example.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class WebSecurityConfig {
    @Resource
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Resource
    private JwtCheckFilter jwtCheckFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain getSecurityFilterChain(HttpSecurity http) throws Exception {

        http.addFilterBefore(jwtCheckFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin().successHandler(customAuthenticationSuccessHandler).permitAll();
        http.formLogin().failureHandler(new CustomAuthenticationFailureHandler());
        http.logout(logout -> {
            logout.logoutSuccessHandler(new CustomLogoutSuccessHandler()); //注销成功时的处理
        });

        //错误处理
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint());//请求未认证的接口
            exception.accessDeniedHandler(new CustomAccessDeniedHandler()); //请求未授权的接口
        });
        //会话管理
        http.sessionManagement(session -> {
            session.maximumSessions(1).expiredSessionStrategy(new CustomSessionInformationExpiredStrategy());
        });

        http.csrf().disable();
        //不创建session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }
}