package com.atguigu.order.config;

import com.atguigu.order.common.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册登录拦截器，拦截所有 /api/** 请求，
 * 排除用户登录和注册接口（这两个接口不需要认证）。
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/user/login",
                        "/api/v1/user/register",
                        "/api/v1/products",                 // 商品分页（首页）
                        "/api/v1/products/category/**",     // 按分类查商品（首页）
                        "/api/v1/categories",               // 分类列表（首页筛选条）
                        "/api/v1/product/*"                 // 商品详情（详情页免登录）
                );
    }
}
