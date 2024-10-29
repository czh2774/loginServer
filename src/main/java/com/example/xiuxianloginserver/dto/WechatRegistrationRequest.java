package com.example.xiuxianloginserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 微信注册请求类，用于封装微信授权注册时提交的授权码信息
 */
@Schema(description = "微信授权注册请求")
public class WechatRegistrationRequest {

    @Schema(description = "微信授权码，用于获取用户的唯一标识符", example = "wx_auth_code_123456", required = true)
    private String wechatCode;

    public WechatRegistrationRequest() {}

    public String getWechatCode() {
        return wechatCode;
    }

    public void setWechatCode(String wechatCode) {
        this.wechatCode = wechatCode;
    }
}
