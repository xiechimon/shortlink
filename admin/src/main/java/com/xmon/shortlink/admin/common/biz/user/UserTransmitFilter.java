package com.xmon.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static com.xmon.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/shortlink/admin/v1/user/login",
            "/api/shortlink/admin/v1/user",
            "/api/shortlink/admin/v1/user/available"
    );

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) 
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String requestURI = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();

        // 忽略登录和注册接口
        if (shouldIgnore(requestURI, method)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 获取请求头中的用户名和token
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");

        // 校验用户名和token是否为空
        if (StrUtil.isBlank(username) || StrUtil.isBlank(token)) {
            returnUnauthorized(httpServletResponse, "用户名或Token为空");
            return;
        }

        // 从Redis中获取用户信息
        String userInfoJsonStr;
        try {
            String redisKey = USER_LOGIN_KEY + username + ":" + token;
            userInfoJsonStr = stringRedisTemplate.opsForValue().get(redisKey);
            
            if (StrUtil.isBlank(userInfoJsonStr)) {
                returnUnauthorized(httpServletResponse, "用户Token不存在或已过期");
                return;
            }
        } catch (Exception e) {
            log.error("从Redis获取用户信息失败，username={}, token={}", username, token, e);
            returnUnauthorized(httpServletResponse, "用户认证失败");
            return;
        }

        // 解析用户信息并存入ThreadLocal
        try {
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr, UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        } catch (Exception e) {
            log.error("解析用户信息失败，userInfoJsonStr={}", userInfoJsonStr, e);
            returnUnauthorized(httpServletResponse, "用户信息格式错误");
            return;
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    /**
     * 判断是否应该忽略该请求
     */
    private boolean shouldIgnore(String requestURI, String method) {
        // 完全匹配忽略的URI
        if (IGNORE_URI.contains(requestURI)) {
            return true;
        }
        // 特殊处理：POST /api/shortlink/admin/v1/user 是注册接口，需要忽略
        return Objects.equals(requestURI, "/api/shortlink/admin/v1/user") && Objects.equals(method, "POST");
    }

    /**
     * 返回401未授权响应
     */
    private void returnUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(new ErrorResponse("A000001", message)));
        writer.flush();
    }

    /**
     * 错误响应
     */
    private static class ErrorResponse {
        public String code;
        public String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}