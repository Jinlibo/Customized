package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.filter.JsonLoginFilter;
import org.example.filter.JwtCheckFilter;
import org.example.handler.CustomAccessDeniedHandler;
import org.example.handler.CustomAuthenticationEntryPoint;
import org.example.handler.CustomLogoutSuccessHandler;
import org.example.service.DBUserDetailsManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class WebSecurityConfig {
    @Resource
    private JwtCheckFilter jwtCheckFilter;
    @Resource
    private DBUserDetailsManager dbUserDetailsManager;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain getSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/auth/login", "/doc.html", "/swagger-ui/**", "/swagger-resources/**", "/v3/**", "/webjars/**").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilterAt(jsonLoginFilter(authenticationManager(http)), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtCheckFilter, UsernamePasswordAuthenticationFilter.class);
        http.logout(logout -> {
            logout.logoutSuccessHandler(new CustomLogoutSuccessHandler()); //注销成功时的处理
        });
        //错误处理
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint());//请求未认证的接口
            exception.accessDeniedHandler(new CustomAccessDeniedHandler()); //请求未授权的接口
        });
        http.csrf().disable();
        //不创建session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 禁用表单登录
        http.formLogin().disable();
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(dbUserDetailsManager) // 注入你的DBUserDetailsManager
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public JsonLoginFilter jsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonLoginFilter filter = new JsonLoginFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/auth/login");
        return filter;
    }
}