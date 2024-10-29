package com.example.xiuxianloginserver.controller;

import com.example.xiuxianloginserver.dto.UserLoginRequest;
import com.example.xiuxianloginserver.dto.UserRegistrationRequest;
import com.example.xiuxianloginserver.dto.WechatRegistrationRequest;
import com.example.xiuxianloginserver.service.AuthService;
import com.example.xiuxianloginserver.util.CustomApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        logger.info("测试初始化完成");
    }

    /**
     * 测试账号密码注册成功的情况
     */
    @Test
    public void testRegister_Success() throws Exception {
        logger.info("测试：账号密码注册 - 成功");

        CustomApiResponse<String> apiResponse = CustomApiResponse.success("注册成功", "mockJwtToken", "/api/auth/register");
        when(authService.register(any(UserRegistrationRequest.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"new_user\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("注册成功")))
                .andExpect(jsonPath("$.data", is("mockJwtToken")));

        logger.info("账号密码注册 - 成功 测试通过");
    }

    /**
     * 测试账号密码注册失败的情况（用户名已存在）
     */
    @Test
    public void testRegister_Failure_UserExists() throws Exception {
        logger.info("测试：账号密码注册 - 用户名已存在");

        CustomApiResponse<String> apiResponse = CustomApiResponse.error(HttpStatus.CONFLICT.value(), "用户名已存在", "/api/auth/register");
        when(authService.register(any(UserRegistrationRequest.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"existing_user\", \"password\": \"password\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("用户名已存在")));

        logger.info("账号密码注册 - 用户名已存在 测试通过");
    }

    /**
     * 测试用户登录成功的情况
     */
    @Test
    public void testLogin_Success() throws Exception {
        logger.info("测试：用户登录 - 成功");

        CustomApiResponse<String> apiResponse = CustomApiResponse.success("登录成功", "mockJwtToken", "/api/auth/login");
        when(authService.login(any(UserLoginRequest.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"existing_user\", \"password\": \"correctPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("登录成功")))
                .andExpect(jsonPath("$.data", is("mockJwtToken")));

        logger.info("用户登录 - 成功 测试通过");
    }

    /**
     * 测试用户登录失败的情况（用户不存在）
     */
    @Test
    public void testLogin_Failure_UserNotFound() throws Exception {
        logger.info("测试：用户登录 - 用户不存在");

        CustomApiResponse<String> apiResponse = CustomApiResponse.error(HttpStatus.BAD_REQUEST.value(), "用户不存在", "/api/auth/login");
        when(authService.login(any(UserLoginRequest.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"non_existing_user\", \"password\": \"password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("用户不存在")));

        logger.info("用户登录 - 用户不存在 测试通过");
    }

    /**
     * 测试用户登录失败的情况（密码错误）
     */
    @Test
    public void testLogin_Failure_IncorrectPassword() throws Exception {
        logger.info("测试：用户登录 - 密码错误");

        CustomApiResponse<String> apiResponse = CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "密码错误", "/api/auth/login");
        when(authService.login(any(UserLoginRequest.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"existing_user\", \"password\": \"wrongPassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("密码错误")));

        logger.info("用户登录 - 密码错误 测试通过");
    }

    /**
     * 测试刷新令牌成功的情况
     */
    @Test
    public void testRefreshToken_Success() throws Exception {
        logger.info("测试：刷新令牌 - 成功");

        String oldToken = "validOldToken";
        CustomApiResponse<String> apiResponse = CustomApiResponse.success("令牌刷新成功", "newMockJwtToken", "/api/auth/refresh-token");
        when(authService.refreshToken(any(String.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", oldToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("令牌刷新成功")))
                .andExpect(jsonPath("$.data", is("newMockJwtToken")));

        logger.info("刷新令牌 - 成功 测试通过");
    }

    /**
     * 测试刷新令牌失败的情况（无效的令牌）
     */
    @Test
    public void testRefreshToken_Failure_InvalidToken() throws Exception {
        logger.info("测试：刷新令牌 - 无效的令牌");

        String invalidToken = "invalidOldToken";
        CustomApiResponse<String> apiResponse = CustomApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "无效的令牌", "/api/auth/refresh-token");
        when(authService.refreshToken(any(String.class))).thenAnswer((Answer<CustomApiResponse<String>>) invocation -> apiResponse);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("无效的令牌")));

        logger.info("刷新令牌 - 无效的令牌 测试通过");
    }
}
