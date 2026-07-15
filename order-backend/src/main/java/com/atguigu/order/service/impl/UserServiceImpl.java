package com.atguigu.order.service.impl;

import com.atguigu.order.bean.User;
import com.atguigu.order.common.BusinessException;
import com.atguigu.order.common.JwtUtils;
import com.atguigu.order.common.R;
import com.atguigu.order.mapper.UserMapper;
import com.atguigu.order.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 * <p>
 * 提供用户注册、登录和信息查询功能。
 * 密码使用 BCrypt 加盐哈希存储，登录时通过 BCryptPasswordEncoder.matches() 校验。
 * 登录成功后生成 JWT Token 返回给前端。
 * </p>
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /** BCrypt 密码编码器（线程安全，可复用实例） */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     * <p>
     * 校验用户名唯一性，使用 BCrypt 加密密码后入库。
     * 新用户默认角色为 USER。
     * </p>
     */
    @Override
    public R<Void> register(User user) {
        // 校验用户名是否已存在
        User exist = userMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        userMapper.insert(user);
        return R.ok("注册成功");
    }

    /**
     * 用户登录
     * <p>
     * 根据用户名查询用户，使用 BCrypt 校验密码。
     * 校验通过后生成包含 userId、username、role 的 JWT Token。
     * </p>
     *
     * @return R.data 为 JWT Token 字符串
     */
    @Override
    public R<String> login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return R.ok("登录成功", token);
    }

    /**
     * 获取当前用户信息
     * 返回时清除密码字段，避免敏感信息泄露。
     */
    @Override
    public R<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 清除密码，防止敏感信息返回前端
        user.setPassword(null);
        return R.ok(user);
    }
}
