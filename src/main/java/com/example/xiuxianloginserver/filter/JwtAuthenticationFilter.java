package com.example.xiuxianloginserver.filter;

import com.example.xiuxianloginserver.common.CustomAuthenticationEntryPoint;
import com.example.xiuxianloginserver.exception.JwtAuthenticationException;
import com.example.xiuxianloginserver.service.CustomUserDetailsService;
import com.example.xiuxianloginserver.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JwtAuthenticationFilter 过滤器，用于在每次请求前验证 JWT。
 * 如果请求中包含有效的 JWT Token，则解析用户身份并设置到 Spring Security 的上下文中。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        // 获取请求的路径
        String requestURI = request.getRequestURI();

        // 判断路径是否需要跳过 JWT 验证
        if (requestURI.startsWith("/api/auth") || requestURI.equals("/login")) {
            logger.debug("Skipping JWT validation for path: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }
        // 获取请求头中的 Authorization 字段
        final String authorizationHeader = request.getHeader("Authorization");
        Long platformUserId = null;
        String jwt = null;

        try {
            // 检查 Authorization 头是否存在且以 Bearer 开头
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                // 提取 JWT Token
                jwt = authorizationHeader.substring(7);
                logger.debug("Extracted JWT Token: {}", jwt);

                // 从 JWT 中解析出用户 ID（platformUserId）
                platformUserId = jwtTokenUtil.getPlatformUserIdFromToken(jwt);
                logger.debug("Extracted platformUserId from JWT: {}", platformUserId);
            } else {
                logger.warn("Authorization header is missing or does not start with Bearer");
            }

            // 如果 platformUserId 存在且当前没有已认证的用户
            if (platformUserId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 使用 platformUserId 从数据库加载用户详细信息
                UserDetails userDetails = customUserDetailsService.loadUserByPlatformUserId(platformUserId);
                logger.debug("Loaded UserDetails for userId: {}", platformUserId);

                // 验证 JWT 的有效性
                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    // JWT 合法，创建认证对象并设置到 SecurityContext 中
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.info("Successfully authenticated userId: {} using JWT", platformUserId);
                } else {
                    logger.warn("Invalid JWT Token for userId: {}", platformUserId);
                }
            } else {
                if (platformUserId == null) {
                    logger.debug("JWT Token is missing, invalid, or userId could not be extracted.");
                } else {
                    logger.debug("User already authenticated with userId: {}", platformUserId);
                }
            }
        } catch (ExpiredJwtException e) {
            // 处理 JWT 过期异常
            logger.error("JWT Token has expired", e);
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Token has expired", e));
            return;
        } catch (SignatureException e) {
            // 处理 JWT 签名异常
            logger.error("Invalid JWT Signature", e);
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Invalid JWT Signature", e));
            return;
        } catch (Exception e) {
            // 捕获其他异常
            logger.error("Error occurred while validating JWT Token", e);
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Invalid JWT Token", e));
            return;
        }

        // 如果没有异常，继续执行过滤器链中的下一个过滤器
        chain.doFilter(request, response);
    }
}
