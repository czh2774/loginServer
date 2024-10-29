package com.example.xiuxianloginserver.common;

import com.example.xiuxianloginserver.util.CustomApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应封装器，自动将所有控制器的响应封装为 ApiResponse 类型。
 * 保证了返回数据的一致性和标准化，并增加了日志记录功能。
 */
@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ApiResponseAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查响应体的类是否属于 OpenAPI 文档类或是已经是 ApiResponse 类型
        String packageName = returnType.getContainingClass().getPackageName();
        if (packageName.startsWith("org.springdoc")) {
            return false;  // 跳过 OpenAPI 文档的响应封装
        }
        // 确保不重复封装已经是 ApiResponse 类型的响应
        return !CustomApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {
        // 排除 SpringDoc 相关路径，避免干扰 OpenAPI 文档的生成
        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body; // 保持 OpenAPI 和 Swagger 的响应不被修改
        }

        // 如果响应体已经是 ApiResponse 类型，直接返回
        if (body instanceof CustomApiResponse) {
            logger.info("Returning existing ApiResponse without modification: {}", body);
            return body;
        }
        if (body == null) {
            body = new Object();  // 确保响应体不为 null
        }
        // 封装为 ApiResponse 并记录日志
        CustomApiResponse<Object> apiResponse = new CustomApiResponse<Object>(HttpStatus.OK.value(), "Success", body,request.getURI().getPath());
        logger.info("Wrapping response into ApiResponse: {}", apiResponse);

        return apiResponse;
    }
}
