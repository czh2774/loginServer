package com.example.xiuxianloginserver.config;

import com.example.xiuxianloginserver.common.CustomAuthenticationEntryPoint;
import com.example.xiuxianloginserver.filter.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    // 密码编码器，使用 BCrypt 加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 认证管理器，用于处理用户的认证请求
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 配置 Spring Security 的过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("开始配置 Security Filter Chain");

        http
                // 禁用 CSRF 保护，适用于 RESTful 风格的接口
                .csrf(csrf -> csrf.disable())
                // 配置跨域资源共享 (CORS)，允许所有域名访问
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(List.of("*")); // 允许所有来源
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的 HTTP 方法
                    corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // 允许的请求头
                    corsConfiguration.setExposedHeaders(List.of("Authorization")); // 允许暴露的响应头
                    return corsConfiguration;
                }))
                // 配置请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许以下路径的匿名访问，通常是用于文档、认证和注册的接口
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/api/auth/**","/login").permitAll()
                        // 对其他所有请求要求认证
                        .anyRequest().authenticated()
                )
                // 配置异常处理器，使用自定义的 AuthenticationEntryPoint 处理未认证的请求
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
                // 将自定义的 JWT 认证过滤器添加到 Spring Security 的过滤链中，放在 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 配置 session 管理，限制每个用户只能有一个会话
                .sessionManagement(sessionManagement ->
                        sessionManagement.maximumSessions(1));

        logger.info("Security Filter Chain 配置完成");
        return http.build();
    }
}
