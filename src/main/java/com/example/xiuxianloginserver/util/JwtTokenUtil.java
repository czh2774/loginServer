package com.example.xiuxianloginserver.util;

import com.example.xiuxianloginserver.exception.JwtAuthenticationException;
import com.example.xiuxianloginserver.security.CustomUserDetails;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    private static final long JWT_TOKEN_VALIDITY = 1000 * 60 * 60 * 10;

    private String secret = "hmx5myP5zY4WOdZtdVIfv/VHJYLeWkwUpppNVTR+tmPEs2EFGlLpp65DdZB6PINFTlyVaLQ3sFzJZxKxAhMpbg==";
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    public String generateToken(Long platformUserId, String authType, String name) {
        return generateToken(platformUserId, authType, name, null);  // 调用重载的 generateToken 方法
    }


    public String generateToken(Long platformUserId, String authType, String name, String platformGlobalId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authType", authType);
        claims.put("platformUserId", platformUserId);
        claims.put("name", name);
        if (platformGlobalId != null) {
            claims.put("platformGlobalId", platformGlobalId);
        }
        logger.debug("Generating token with claims: {}", claims);
        return doGenerateToken(claims, platformUserId.toString());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            // 从 token 中提取用户名 (或其他标识符)
            final Long platformUserId = getPlatformUserIdFromToken(token);

            // 验证从 token 中提取的用户名与传入的用户详情中的用户名是否一致
            if (platformUserId == null || !platformUserId.equals(((CustomUserDetails) userDetails).getPlatformUserId())) {
                throw new JwtException("PlatformUserId in JWT token does not match the authenticated user");
            }

            // 验证 token 是否过期
            if (isTokenExpired(token)) {
                throw new ExpiredJwtException(null, null, "Token has expired");
            }

            // 如果所有验证通过，返回 true 表示 token 合法
            return true;

        } catch (ExpiredJwtException e) {
            // 抛出已过期的 JWT 异常
            throw new ExpiredJwtException(null, null, "JWT token has expired", e);
        } catch (SecurityException  e) {
            // 抛出签名无效异常
            throw new SecurityException ("Invalid JWT signature", e);
        } catch (JwtException e) {
            // 捕获任何其他 JWT 相关异常并抛出
            throw new JwtException("JWT token is invalid", e);
        } catch (Exception e) {
            // 捕获并抛出其他非 JWT 异常
            throw new RuntimeException("An error occurred while validating the JWT token", e);
        }
    }







    public String getAuthTypeFromToken(String token) {
        logger.debug("Extracting authType from token: {}", token);
        return getClaimFromToken(token, claims -> claims.get("authType", String.class));
    }

    public Long getPlatformUserIdFromToken(String token) {
        logger.debug("Extracting platformUserId from token: {}", token);
        return getClaimFromToken(token, claims -> claims.get("platformUserId", Long.class));
    }


    public String getPlatformGlobalIdFromToken(String token) {
        logger.debug("Extracting platformGlobalId from token: {}", token);
        return getClaimFromToken(token, claims -> claims.get("platformGlobalId", String.class));
    }

    public String getNameFromToken(String token) {
        logger.debug("Extracting name from token: {}", token);
        return getClaimFromToken(token, claims -> claims.get("name", String.class));
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        long currentTimeMillis = System.currentTimeMillis();
        Date issuedAt = new Date(currentTimeMillis);
        Date expiration = new Date(currentTimeMillis + JWT_TOKEN_VALIDITY);

        logger.debug("Generating JWT token for subject: {}, issued at: {}, expires at: {}", subject, issuedAt, expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)  // 10 hours
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }


    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        logger.debug("Checking if token is expired: {}, expiration: {}", token, expiration);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        logger.debug("Extracting expiration date from token: {}", token);
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            logger.debug("Parsing claims from token: {}", token);

            // 将你的 secret 转换为 HMAC-SHA 密钥
            Key key = Keys.hmacShaKeyFor(secret.getBytes());

            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key) // 使用 HMAC-SHA 密钥进行签名验证
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            logger.debug("Token iat: {}, exp: {}", claims.getIssuedAt(), claims.getExpiration());
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            logger.error("Token has expired: {}", e.getMessage());
            throw new JwtAuthenticationException("Token has expired", e); // 使用自定义异常类
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT signature", e); // 使用自定义异常类
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token", e); // 使用自定义异常类
        } catch (Exception e) {
            logger.error("Error parsing token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e); // 可以自定义异常类型
        }
    }



    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
}
