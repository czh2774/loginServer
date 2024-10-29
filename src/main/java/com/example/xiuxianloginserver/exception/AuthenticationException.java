package com.example.xiuxianloginserver.exception;

/**
 * 自定义异常类，用于处理认证失败的情况。
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
