package com.example.xiuxianloginserver.exception;

/**
 * 自定义异常类，用于表示资源未找到的情况。
 */
public class ResourceNotFoundException extends RuntimeException {

    // 构造方法
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
