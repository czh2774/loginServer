package com.example.xiuxianloginserver.util;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * 通用 API 响应类，用于标准化服务端返回给客户端的数据。
 *
 * @param <T> 响应数据的类型。
 */
public class CustomApiResponse<T> {

    private LocalDateTime timestamp;  // 响应的时间戳
    private int status;        // HTTP 状态码
    private String message;           // 响应的消息
    private T data;                   // 响应的数据
    private String path;              // 请求的路径

    // 手动定义的构造函数，包含所有字段
    public CustomApiResponse(LocalDateTime timestamp, int status, String message, T data, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.data = data;
        this.path = path;
    }

    // 只包含 status, message, data 和 path 的简化构造函数（自动设置 timestamp）
    public CustomApiResponse(int status, String message, T data, String path) {
        this(LocalDateTime.now(), status, message, data, path);
    }

    // 静态方法，用于生成错误响应
    public static <T> CustomApiResponse<T> error(int status, String message, String path) {
        return new CustomApiResponse<>(LocalDateTime.now(), status, message, null, path);
    }

    // 静态方法，用于生成成功响应
    public static <T> CustomApiResponse<T> success(String message, T data, String path) {
        return new CustomApiResponse<>(LocalDateTime.now(), HttpStatus.OK.value(), message, data, path);
    }

    // Getters 和 Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
