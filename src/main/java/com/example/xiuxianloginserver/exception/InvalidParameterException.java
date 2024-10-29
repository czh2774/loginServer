package com.example.xiuxianloginserver.exception;

/**
 * 自定义异常类，用于表示无效的参数。
 */
public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
