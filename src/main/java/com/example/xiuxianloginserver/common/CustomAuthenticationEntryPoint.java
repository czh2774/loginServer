package com.example.xiuxianloginserver.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        // 增加日志记录来捕捉异常信息
        logger.error("Authentication exception: {}", authException.getMessage());

        // 设置响应的状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 初始化 ObjectMapper 并配置时间模块
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 构建错误信息
        String errorMessage;
        if (authException.getMessage().contains("Token has expired")) {
            logger.warn("Token has expired");
            errorMessage = "Token has expired";
        } else if (authException.getMessage().contains("Invalid JWT Signature")) {
            logger.warn("Invalid JWT Signature");
            errorMessage = "Invalid JWT Signature";
        } else {
            logger.warn("Invalid JWT Token");
            errorMessage = "Invalid JWT Token";
        }

        // 创建响应对象
        ApiResponse apiResponse = new ApiResponse(
                "UNAUTHORIZED",
                errorMessage,
                request.getRequestURI()
        );

        // 将响应对象转换为 JSON 并写入响应体
        try (PrintWriter writer = response.getWriter()) {
            writer.write(mapper.writeValueAsString(apiResponse));
            writer.flush();
        }
    }

    // 定义 ApiResponse 类来封装响应信息
    public static class ApiResponse {
        private String status;
        private String message;
        private String path;

        public ApiResponse(String status, String message, String path) {
            this.status = status;
            this.message = message;
            this.path = path;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
