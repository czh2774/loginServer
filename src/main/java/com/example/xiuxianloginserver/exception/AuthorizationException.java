package com.example.xiuxianloginserver.exception;

/**
 * 自定义异常类，用于处理权限不足的情况。
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
