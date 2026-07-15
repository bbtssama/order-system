package com.atguigu.order.mapper;

import com.atguigu.order.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    /** 新增用户（回填自增ID） */
    int insert(User user);

    /** 根据用户名查询用户（登录时使用） */
    User selectByUsername(@Param("username") String username);

    /** 根据ID查询用户 */
    User selectById(@Param("id") Long id);
}
