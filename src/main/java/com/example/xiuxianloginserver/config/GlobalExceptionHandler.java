package com.example.xiuxianloginserver.config;

import com.example.xiuxianloginserver.exception.AuthenticationException;
import com.example.xiuxianloginserver.exception.ResourceNotFoundException;
import com.example.xiuxianloginserver.util.CustomApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 资源未找到异常处理。
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.error("资源未找到: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * 处理认证异常。
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logger.warn("认证失败: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * 通用异常处理。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {
        logger.error("服务器内部错误: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "服务器发生未知错误", request);
    }

    private ResponseEntity<CustomApiResponse<Object>> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        CustomApiResponse<Object> response = new CustomApiResponse<>(status.value(), message, null, request.getRequestURI());
        return new ResponseEntity<>(response, status);
    }
}
