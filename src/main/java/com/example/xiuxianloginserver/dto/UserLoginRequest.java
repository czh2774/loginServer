package com.example.xiuxianloginserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户登录请求类，用于封装用户账号密码登录时提交的信息
 */
@Schema(description = "用户登录请求")
public class UserLoginRequest {

    @Schema(description = "用户账号，用于标识用户", example = "user_001", required = true)
    private String username;

    @Schema(description = "用户密码，明文密码将会在后端进行加密处理", example = "mySecurePassword", required = true)
    private String password;

    public UserLoginRequest() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
