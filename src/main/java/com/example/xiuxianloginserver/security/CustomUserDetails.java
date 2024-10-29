package com.example.xiuxianloginserver.security;

import com.example.xiuxianloginserver.models.UserModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final UserModel user;

    public CustomUserDetails(UserModel user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 假设每个用户都具有 "ROLE_USER" 权限，如果有更多权限，可以在这里返回
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // 返回加密后的密码
    }

    @Override
    public String getUsername() {
        return user.getUserName();  // 返回用户名
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 假设账户没有过期，具体逻辑可以根据实际需求修改
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isBanned();  // 检查用户是否被锁定或禁用
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 假设凭证没有过期，具体逻辑可以根据实际需求修改
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();  // 检查用户是否启用
    }

    public Long getPlatformUserId() {
        return user.getPlatformUserId();  // 返回平台用户 ID
    }

    public String getAuthType() {
        return user.getAuthType();  // 返回认证类型（如密码、微信等）
    }

    public String getNickName() {
        return user.getNickName();  // 返回用户昵称
    }

    // 其他自定义方法，您可以根据 UserModel 的字段进一步扩展
}
