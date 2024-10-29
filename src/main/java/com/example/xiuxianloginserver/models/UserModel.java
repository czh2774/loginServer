package com.example.xiuxianloginserver.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 用户模型类，包含用户基本信息、认证类型和状态
 */
@Entity
@Table(name = "users")
@Data
@Schema(description = "用户实体类，包含用户的基本信息、认证类型、状态信息等")
public class UserModel {


    @Id
    @Column(nullable = false, unique = true, length = 64)
    @Schema(description = "平台用户唯一标识符，雪花ID", example = "user_001", required = true)
    private Long platformUserId; // 平台用户唯一标识符

    @Column(length = 64)
    @Schema(description = "跨平台唯一标识符，微信授权注册时为unionid，其他情况为空", example = "global_user_001")
    private String platformGlobalId; // 跨平台唯一标识符

    @Column(nullable = false, unique = true, length = 64)
    @Schema(description = "用户登录账号（用户名），用户自己创建，最大长度64字符", example = "user_001", required = true)
    private String userName;  // 用户账号，用于登录的用户名

    @Column(nullable = false, length = 64)
    @Schema(description = "用户名称（昵称），可用于展示，最大长度64字符", example = "玩家001", required = true)
    private String nickName; // 用户名称

    @Column(nullable = false, length = 255)
    @Schema(description = "用户密码，账号密码注册时存储加密后的密码，微信授权注册可为空", example = "encrypted_password", required = false)
    private String password; // 用户密码

    @Column(length = 512)
    @Schema(description = "JWT Token，仅用于存储最后一次生成的令牌，可为空", example = "eyJhbGciOiJIUzI1NiIsInR...")
    private String jwtToken; // JWT Token

    @Column(length = 255)
    @Schema(description = "用户的刷新令牌，用于延长会话的有效期", example = "refresh_token_example")
    private String refreshToken; // 刷新令牌

    @Column(nullable = false)
    @Schema(description = "用户启用状态，true表示启用，false表示禁用", example = "true", required = true)
    private boolean enabled = true; // 是否启用，默认启用

    @Column(nullable = false)
    @Schema(description = "用户封禁状态，true表示封禁，false表示未封禁", example = "false", required = true)
    private boolean banned = false; // 是否封禁，默认未封禁

    @Column(nullable = false, length = 32)
    @Schema(description = "用户认证类型，标识用户的注册来源类型，例如'account_password'或'wechat'", example = "account_password", required = true)
    private String authType; // 用户认证类型：account_password 或 wechat

    @CreationTimestamp
    @Column(updatable = false)
    @Schema(description = "账户创建时间，自动生成，更新时不变", example = "2024-10-14T00:00:00")
    private LocalDateTime createdAt; // 账户创建时间

    @UpdateTimestamp
    @Column
    @Schema(description = "上次登录时间，每次更新自动刷新", example = "2024-10-14T12:34:56")
    private LocalDateTime lastLogin; // 上次登录时间
}
