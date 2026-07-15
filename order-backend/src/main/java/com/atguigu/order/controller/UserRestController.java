package com.atguigu.order.controller;

import com.atguigu.order.bean.User;
import com.atguigu.order.common.R;
import com.atguigu.order.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * <p>
 * 提供用户注册、登录和信息查询接口。
 * 注册和登录为公开接口（不需要认证），获取用户信息需登录。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin
public class UserRestController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册（公开接口）
     * @param user 包含 username、password、nickname、email、phone 的用户信息
     */
    @PostMapping("/register")
    public R<Void> register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 用户登录（公开接口）
     * @param loginData 包含 username 和 password 的 Map
     * @return R.data 为 JWT Token 字符串
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        return userService.login(username, password);
    }

    /**
     * 获取当前登录用户信息（需认证）
     * @return R.data 为 User 对象（不含密码）
     */
    @GetMapping("/info")
    public R<User> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getUserInfo(userId);
    }
}
