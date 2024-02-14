package com.fengrui.shortlink.admin.common.biz.user;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fengrui.shortlink.common.convention.exception.ClientException;
import com.fengrui.shortlink.common.convention.result.Results;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.fengrui.shortlink.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode.USER_TOKEN_FAIL;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {
    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/short-link/admin/users/login",
            "/api/short-link/admin/users/has-username",
            "/api/short-link/admin/users/register",
            "/doc.html",
            "/v3/api-docs/swagger-config"
    );

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if (!(IGNORE_URI.contains(requestURI) || requestURI.startsWith("/v3") || requestURI.startsWith("/webjars"))) {
            String username = httpServletRequest.getHeader("username");
            String token = httpServletRequest.getHeader("token");
            if (!StrUtil.isAllNotBlank(username, token)) {
                returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL))));
                return;
            }
            Object userInfoJsonStr;
            try {
                userInfoJsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
                if (userInfoJsonStr == null) {
                    throw new ClientException(USER_TOKEN_FAIL);
                }
            } catch (Exception ex) {
                returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL))));
                return;
            }
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }

}
