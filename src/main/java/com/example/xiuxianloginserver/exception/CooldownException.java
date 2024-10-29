package com.example.xiuxianloginserver.exception;

/**
 * 自定义异常类，用于表示冷却时间未结束的情况。
 */
public class CooldownException extends RuntimeException {

    public CooldownException(String message) {
        super(message);
    }

    public CooldownException(String message, Throwable cause) {
        super(message, cause);
    }
}
