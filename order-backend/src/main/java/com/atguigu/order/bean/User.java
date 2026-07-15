package com.atguigu.order.bean;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 对应数据库 user 表。role 字段区分角色：USER（普通用户）、ADMIN（管理员）。
 * </p>
 */
@Data
public class User {
    /** 主键ID */
    private Long id;
    /** 用户名（唯一） */
    private String username;
    /** 密码（BCrypt 加密存储，不返回前端） */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 角色：USER / ADMIN */
    private String role;
    /** 创建时间 */
    private LocalDateTime createTime;
}
