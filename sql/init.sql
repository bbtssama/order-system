SET NAMES utf8mb4;
-- 订单管理系统数据库初始化脚本
CREATE DATABASE IF NOT EXISTS order_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE order_db;

-- 用户表
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品表
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    image_url VARCHAR(500),
    status VARCHAR(10) NOT NULL DEFAULT 'ON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 购物车表
CREATE TABLE cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单项表
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== 测试数据 ==========

/** 密码均为BCrypt加密：admin 的密码为 admin123，user1/user2 的密码为 user123 */
INSERT INTO `user` (username, password, nickname, email, phone, role) VALUES
('admin', '$2a$10$LDfJ8aNRUggmPd6TwhdYm.bmANLs3bSnDDACPp5KWRKuHfxR.CAEe', '管理员', 'admin@order.com', '13800000001', 'ADMIN'),
('user1', '$2a$10$N3fykouE6WtFgv15OQZN6OwEHw6ZJORohGctykJtrbmFaKa0E6ZGi', '用户一', 'user1@order.com', '13800000002', 'USER'),
('user2', '$2a$10$N3fykouE6WtFgv15OQZN6OwEHw6ZJORohGctykJtrbmFaKa0E6ZGi', '用户二', 'user2@order.com', '13800000003', 'USER');

INSERT INTO category (id, name) VALUES
(1, '电子产品'),
(2, '服装鞋帽'),
(3, '图书音像'),
(4, '家居用品'),
(5, '食品饮料');

INSERT INTO product (id, name, description, price, stock, category_id, image_url, status) VALUES
(1, '无线蓝牙耳机', '高品质降噪蓝牙耳机，续航长达30小时', 299.00, 100, 1, '/images/headphone.jpg', 'ON'),
(2, '机械键盘RGB', '青轴机械键盘，104键全键无冲', 459.00, 50, 1, '/images/keyboard.jpg', 'ON'),
(3, '男士运动跑鞋', '透气网面跑步鞋，轻便舒适', 399.00, 200, 2, '/images/shoes.jpg', 'ON'),
(4, '女士休闲连衣裙', '2024新款夏季修身连衣裙', 259.00, 150, 2, '/images/dress.jpg', 'ON'),
(5, 'Java编程思想', '经典Java技术书籍，第五版', 108.00, 300, 3, '/images/java-book.jpg', 'ON'),
(6, '数据结构与算法', '程序员必备算法书', 89.00, 250, 3, '/images/algorithm-book.jpg', 'ON'),
(7, '简约落地灯', '北欧风格客厅落地灯', 589.00, 30, 4, '/images/lamp.jpg', 'ON'),
(8, '记忆棉枕头', '慢回弹护颈枕，助眠好睡眠', 199.00, 180, 4, '/images/pillow.jpg', 'ON'),
(9, '坚果礼盒装', '每日坚果混合装30包', 128.00, 500, 5, '/images/nuts.jpg', 'ON'),
(10, '有机绿茶礼盒', '明前龙井茶200g精致礼盒', 268.00, 80, 5, '/images/tea.jpg', 'ON');

-- 购物车测试数据（user1的购物车）
INSERT INTO cart_item (user_id, product_id, quantity) VALUES
(2, 1, 2),
(2, 5, 1),
(3, 3, 1);

-- 订单测试数据
INSERT INTO `order` (id, order_no, user_id, total_amount, status, create_time, update_time) VALUES
(1, 'ORD20240625000001', 2, 706.00, 'COMPLETED', '2024-06-25 10:30:00', '2024-06-28 12:00:00'),
(2, 'ORD20240627000002', 2, 459.00, 'SHIPPED', '2024-06-27 14:20:00', '2024-06-28 09:00:00'),
(3, 'ORD20240628000003', 3, 399.00, 'PENDING', '2024-06-28 08:15:00', '2024-06-28 08:15:00');

INSERT INTO order_item (order_id, product_id, product_name, product_price, quantity, total_price) VALUES
(1, 1, '无线蓝牙耳机', 299.00, 2, 598.00),
(1, 5, 'Java编程思想', 108.00, 1, 108.00),
(2, 2, '机械键盘RGB', 459.00, 1, 459.00),
(3, 3, '男士运动跑鞋', 399.00, 1, 399.00);
