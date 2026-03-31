package com.xmon.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static com.xmon.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 *
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    public final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");
        String userInfoJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_KEY + username + ":" + token);
        if (userInfoJson != null) {
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJson, UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}