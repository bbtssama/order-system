package com.atguigu.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单管理系统启动类
 * <p>
 * SpringBoot 应用入口，通过 @SpringBootApplication 开启自动配置和组件扫描。
 * 启动后默认监听 8081 端口（可在 application.properties 中配置）。
 * </p>
 */
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
