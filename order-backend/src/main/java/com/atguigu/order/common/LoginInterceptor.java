package com.atguigu.order.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 * <p>
 * 拦截 /api/** 路径下的所有请求（登录和注册接口除外），校验 JWT Token。
 * 认证通过后，将 userId、username、role 注入到请求属性中，
 * 供后续 Controller 通过 request.getAttribute() 获取当前用户信息。
 * </p>
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取 Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 校验 Token 有效性
        if (!jwtUtils.validateToken(token)) {
            writeUnauthorized(response, "Token无效或已过期");
            return false;
        }

        // 将用户信息注入请求属性，供 Controller 使用
        request.setAttribute("userId", jwtUtils.getUserIdFromToken(token));
        request.setAttribute("username", jwtUtils.getUsernameFromToken(token));
        request.setAttribute("role", jwtUtils.getRoleFromToken(token));

        return true;
    }

    /**
     * 统一写入 401 未授权响应
     */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\"}");
    }
}
