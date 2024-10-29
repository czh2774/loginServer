package com.example.xiuxianloginserver.service;

import cn.hutool.core.lang.Snowflake;
import com.example.xiuxianloginserver.dto.UserLoginRequest;
import com.example.xiuxianloginserver.dto.UserRegistrationRequest;
import com.example.xiuxianloginserver.exception.InvalidLoginException;
import com.example.xiuxianloginserver.exception.InvalidTokenException;
import com.example.xiuxianloginserver.exception.UserAlreadyExistsException;
import com.example.xiuxianloginserver.exception.UserNotFoundException;
import com.example.xiuxianloginserver.models.UserModel;
import com.example.xiuxianloginserver.repository.UserRepository;
import com.example.xiuxianloginserver.util.CustomApiResponse;
import com.example.xiuxianloginserver.util.JwtTokenUtil;
import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);  // 生成唯一ID

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    public AuthService(UserRepository userRepository, JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // 注册用户
    public String register(UserRegistrationRequest request) {
        logger.debug("开始处理账号密码注册请求：{}", request.getUsername());

        // 检查用户名是否已存在
        if (userRepository.findByUserName(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("用户名已存在");
        }

        // 加密密码并创建用户
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Long platformUserId = snowflake.nextId();  // 生成 Long 类型的雪花ID

        UserModel user = new UserModel();
        user.setPlatformUserId(platformUserId);
        user.setUserName(request.getUsername());
        user.setNickName(request.getUsername());
        user.setPassword(encodedPassword);
        user.setAuthType("account_password");

        // 生成 JWT 令牌
        String jwtToken = jwtTokenUtil.generateToken(user.getPlatformUserId(), user.getAuthType(), user.getUserName());
        user.setJwtToken(jwtToken);

        userRepository.save(user);
        logger.debug("注册成功，生成的JWT令牌：{}", jwtToken);

        return jwtToken;
    }

    // 用户登录
    public UserModel login(UserLoginRequest request) {
        logger.debug("开始处理用户登录请求：{}", request.getUsername());

        // 查找用户
        Optional<UserModel> userOptional = userRepository.findByUserName(request.getUsername());
        if (userOptional.isEmpty()) {
            throw new InvalidLoginException("用户不存在");
        }

        UserModel user = userOptional.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidLoginException("密码错误");
        }

        // 生成新的 JWT 令牌
        String jwtToken = jwtTokenUtil.generateToken(user.getPlatformUserId(), user.getAuthType(), user.getUserName());
        user.setJwtToken(jwtToken);  // 更新用户对象中的 JWT 令牌

        // 如果你有特殊需求，比如在登录时需要记录令牌，则保存用户信息
        // userRepository.save(user);

        logger.debug("登录成功，生成的JWT令牌：{}", jwtToken);

        // 返回包含 JWT Token 的 UserModel 对象
        return user;
    }


    // 刷新令牌
    public String refreshToken(String oldToken) {
        logger.debug("开始处理令牌刷新请求");

        Long platformUserId = jwtTokenUtil.getPlatformUserIdFromToken(oldToken);
        Optional<UserModel> userOptional = userRepository.findById(platformUserId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("用户不存在");
        }

        UserModel user = userOptional.get();
        String newJwtToken = jwtTokenUtil.generateToken(user.getPlatformUserId(), user.getAuthType(), user.getUserName());
        user.setJwtToken(newJwtToken);

        userRepository.save(user);
        logger.debug("令牌刷新成功，生成的新JWT令牌：{}", newJwtToken);

        return newJwtToken;
    }

    // 验证令牌
    public Map<String, Object> validateToken(String token) {
        try {
            // 从令牌中提取用户 ID
            Long platformUserId = jwtTokenUtil.getPlatformUserIdFromToken(token);
            logger.debug("从令牌中解析出用户 ID: {}", platformUserId);

            // 使用 platformUserId 加载 UserDetails
            UserDetails userDetails = userDetailsService.loadUserByPlatformUserId(platformUserId);
            logger.debug("加载的用户详情: {}", userDetails);

            // 验证 JWT 令牌的有效性
            boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
            if (!isValid) {
                throw new InvalidTokenException("JWT 令牌无效或已过期");
            }

            // 返回令牌有效的响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", platformUserId);
            responseData.put("username", userDetails.getUsername());

            return responseData;

        } catch (JwtException e) {
            logger.error("JWT 验证时发生异常: {}", e.getMessage());
            throw new InvalidTokenException("令牌无效或解析失败");
        }
    }
}
