package org.example.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.example.vo.CommonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;


/**
 * 全局异常处理器
 *
 * @author water
 * @date 2023/1/10
 */
@SuppressWarnings("all")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理登录异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public CommonResponse handleLoginException(BadCredentialsException e) {
        log.error("登录失败 - 错误: {}", e.getMessage());
        return CommonResponse.error(401, "登录失败：" + e.getMessage());
    }

    /**
     * 处理访问被拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public CommonResponse<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error("没有权限 - 错误: {}", e.getMessage());
        return CommonResponse.error(403, "没有权限");
    }

    /**
     * /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public CommonResponse<String> handleServiceException(ServiceException e, HttpServletRequest request) {
        Integer code = e.getCode();
        return Objects.nonNull(code) ? CommonResponse.error(code, e.getMessage()) : CommonResponse.error(e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public CommonResponse<String> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生异常", requestURI);
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause != null) {
            log.error("根因异常类型：{}", rootCause.getClass().getName());
        }
        String stackTrace = ExceptionUtils.getStackTrace(e);
        log.error("异常堆栈信息:{}", stackTrace);
        return CommonResponse.error(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResponse<String> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生异常", requestURI);
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause != null) {
            log.error("根因异常类型：{}", rootCause.getClass().getName());
        }
        String stackTrace = ExceptionUtils.getStackTrace(e);
        log.error("异常堆栈信息:{}", stackTrace);
        return CommonResponse.error(e.getMessage());
    }

    /**
     * 自定义验证异常处理
     */
    @ExceptionHandler(BindException.class)
    public CommonResponse<String> handleBindException(BindException e, HttpServletRequest request) {
        log.info("handleBindException,请求地址:{},Message:{}", request.getRequestURI(), ExceptionUtils.getRootCauseMessage(e));
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String message = String.format("参数验证失败: %s",
                fieldError.getDefaultMessage());
        return CommonResponse.error(message);
    }

    /**
     * 方法参数验证异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.info("handleMethodArgumentNotValidException,请求地址:{},Message:{}", request.getRequestURI(), ExceptionUtils.getRootCauseMessage(e));
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null) {
            String message = String.format("参数验证失败: %s",
                    fieldError.getDefaultMessage());
            return CommonResponse.error(message);
        }
        return CommonResponse.error("参数验证失败");
    }

    /**
     * 方法参数验证异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResponse<String> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.info("handleConstraintViolationException,请求地址:{},Message:{}", request.getRequestURI(), ExceptionUtils.getRootCauseMessage(e));
        // 获取第一个验证错误
        ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
        if (violation != null) {
            String message = String.format("参数验证失败: %s",
                    violation.getMessage());
            return CommonResponse.error(message);
        }
        return CommonResponse.error("参数验证失败");
    }


    /**
     * 处理 JSON 转换失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResponse<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        log.info("handleHttpMessageNotReadableException,请求地址:{},Message:{}", request.getRequestURI(), ExceptionUtils.getRootCauseMessage(ex));
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldName = ife.getPathReference(); // 可提取字段路径
            String inputValue = ife.getValue().toString();
            String targetType = ife.getTargetType().getSimpleName();
            return CommonResponse.error("数据类型错误，输入{" + inputValue + "}应为 " + targetType + " 类型");
        }
        if (cause instanceof MismatchedInputException) {
            MismatchedInputException mie = (MismatchedInputException) cause;
            return CommonResponse.error("请求 JSON 字段结构不匹配：" + mie.getPathReference());
        }
        // 默认错误信息
        return CommonResponse.error("请求数据格式有误，请检查数据类型、结构或格式");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public CommonResponse<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.info("handleMaxUploadSizeExceededException,请求地址:{},Message:{}", request.getRequestURI(), ExceptionUtils.getRootCauseMessage(e));
        return CommonResponse.error("上传文件大小超过限制");
    }

}
