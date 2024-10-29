package com.example.xiuxianloginserver.service;

import com.example.xiuxianloginserver.models.UserModel;
import com.example.xiuxianloginserver.repository.UserRepository;
import com.example.xiuxianloginserver.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查找用户
        Optional<UserModel> user = userRepository.findByUserName(username);
        // 如果找不到用户，抛出异常
        return user.map(CustomUserDetails::new).orElseThrow(() ->
                new UsernameNotFoundException("用户未找到: " + username));
    }

    // 新增方法：通过平台用户 ID 加载用户
    public UserDetails loadUserByPlatformUserId(Long platformUserId) throws UsernameNotFoundException {
        // 根据平台用户ID查找用户
        Optional<UserModel> user = userRepository.findById(platformUserId);
        // 如果找不到用户，抛出异常
        return user.map(CustomUserDetails::new).orElseThrow(() ->
                new UsernameNotFoundException("用户未找到: " + platformUserId));
    }
}
