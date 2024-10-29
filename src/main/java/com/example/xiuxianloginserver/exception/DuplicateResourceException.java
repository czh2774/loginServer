package com.example.xiuxianloginserver.exception;

/**
 * 自定义异常类，用于表示资源重复的情况。
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
