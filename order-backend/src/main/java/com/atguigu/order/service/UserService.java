package com.atguigu.order.service;

import com.atguigu.order.bean.User;
import com.atguigu.order.common.R;

/**
 * 用户服务接口
 */
public interface UserService {

    /** 用户注册 */
    R<Void> register(User user);

    /** 用户登录，返回 JWT Token */
    R<String> login(String username, String password);

    /** 获取用户信息 */
    R<User> getUserInfo(Long userId);
}
