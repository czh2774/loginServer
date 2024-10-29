package com.example.xiuxianloginserver.controller;

import com.example.xiuxianloginserver.dto.UserLoginRequest;
import com.example.xiuxianloginserver.dto.UserRegistrationRequest;
import com.example.xiuxianloginserver.exception.InvalidLoginException;
import com.example.xiuxianloginserver.exception.InvalidTokenException;
import com.example.xiuxianloginserver.exception.UserAlreadyExistsException;
import com.example.xiuxianloginserver.models.UserModel;
import com.example.xiuxianloginserver.service.AuthService;
import com.example.xiuxianloginserver.util.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "认证控制器")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Operation(summary = "账号密码注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "无效的请求"),
            @ApiResponse(responseCode = "409", description = "用户名已存在")
    })
    @PostMapping("/register")
    public ResponseEntity<CustomApiResponse<String>> register(@RequestBody UserRegistrationRequest request, HttpServletRequest httpRequest) {
        logger.info("用户尝试注册: {}", request.getUsername());
        try {
            String token = authService.register(request);
            return ResponseEntity.ok(CustomApiResponse.success("注册成功", token, httpRequest.getRequestURI()));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CustomApiResponse.error(HttpStatus.CONFLICT.value(), e.getMessage(), httpRequest.getRequestURI()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误", httpRequest.getRequestURI()));
        }
    }

    @Operation(summary = "用户登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "无效的请求"),
            @ApiResponse(responseCode = "401", description = "认证失败")
    })
    @PostMapping("/login")
    public ResponseEntity<CustomApiResponse<UserModel>> login(@RequestBody UserLoginRequest request, HttpServletRequest httpRequest) {
        // 打印请求体内容
        logger.info("用户尝试登录: 用户名 = {}, 密码 = {}", request.getUsername(), request.getPassword());

        try {
            // 调用 authService 登录，并返回 UserModel
            UserModel userModel = authService.login(request);

            if (userModel != null) {
                // 返回包含用户信息和 Token 的响应
                return ResponseEntity.ok(CustomApiResponse.success("登录成功", userModel, httpRequest.getRequestURI()));
            } else {
                // 如果登录失败，则返回 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误", httpRequest.getRequestURI()));
            }
        } catch (InvalidLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), httpRequest.getRequestURI()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误", httpRequest.getRequestURI()));
        }
    }




    @Operation(summary = "刷新令牌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "令牌刷新成功"),
            @ApiResponse(responseCode = "401", description = "无效的令牌"),
            @ApiResponse(responseCode = "403", description = "令牌已过期")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<CustomApiResponse<?>> refreshToken(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            HttpServletRequest httpRequest) {

        logger.debug("Authorization Header: {}", authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            authorizationHeader = authorizationHeader.substring(7); // 去除 Bearer 前缀
        }

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            logger.warn("缺少 Authorization 头或无效");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CustomApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Authorization header is missing or invalid", httpRequest.getRequestURI()));
        }

        try {
            String newToken = authService.refreshToken(authorizationHeader);
            return ResponseEntity.ok(CustomApiResponse.success("令牌刷新成功", newToken, httpRequest.getRequestURI()));
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), httpRequest.getRequestURI()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误", httpRequest.getRequestURI()));
        }
    }

    @Operation(summary = "验证 JWT 令牌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "令牌有效"),
            @ApiResponse(responseCode = "401", description = "无效的令牌或认证失败")
    })
    @GetMapping("/validate-token")
    public ResponseEntity<CustomApiResponse<Map<String, Object>>> validateToken(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, HttpServletRequest httpRequest) {
        logger.debug("Received token for validation: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("无效或缺少 Authorization 头");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "无效的令牌", httpRequest.getRequestURI()));
        }

        String token = authorizationHeader.substring(7);

        try {
            Map<String, Object> userInfo = authService.validateToken(token);
            return ResponseEntity.ok(CustomApiResponse.success("令牌有效", userInfo, httpRequest.getRequestURI()));
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), httpRequest.getRequestURI()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误", httpRequest.getRequestURI()));
        }
    }

}
