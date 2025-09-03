package org.example.interceptor;

import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyAsyncInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        String authorization1 = request.getHeader("authorization");
        System.out.println("Authorization: " + authorization);
        System.out.println("Authorization1: " + authorization1);
        System.out.println("1. async-interceptor-preHandle - " + Thread.currentThread().getName());
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        String authorization1 = request.getHeader("authorization");
        System.out.println("Authorization: " + authorization);
        System.out.println("Authorization1: " + authorization1);
        System.out.println("2. async-interceptor-afterConcurrentHandlingStarted - " + Thread.currentThread().getName());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        String authorization = request.getHeader("Authorization");
        String authorization1 = request.getHeader("authorization");
        System.out.println("Authorization: " + authorization);
        System.out.println("Authorization1: " + authorization1);
        System.out.println("3. async-interceptor-postHandle - " + Thread.currentThread().getName());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        String authorization = request.getHeader("Authorization");
        String authorization1 = request.getHeader("authorization");
        System.out.println("Authorization: " + authorization);
        System.out.println("Authorization1: " + authorization1);
        System.out.println("4. async-interceptor-afterCompletion - " + Thread.currentThread().getName());
    }
}