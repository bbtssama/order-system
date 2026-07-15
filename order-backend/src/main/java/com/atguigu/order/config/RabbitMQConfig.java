package com.atguigu.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * <p>
 * 声明订单通知相关的队列、交换机和绑定关系，采用死信队列模式：
 * <ul>
 *   <li>主队列 order.notify.queue：接收订单通知消息，设置 TTL 30s 和最大长度 1000</li>
 *   <li>死信队列 order.notify.dlx.queue：消息超时或被拒绝后进入此队列，便于后续排查</li>
 *   <li>使用 DirectExchange 精确匹配路由键</li>
 * </ul>
 * 队列声明为 durable（持久化），防止 RabbitMQ 重启后消息丢失。
 * </p>
 */
@Configuration
public class RabbitMQConfig {

    /** 订单通知队列（主队列） */
    public static final String ORDER_NOTIFY_QUEUE = "order.notify.queue";
    /** 死信队列 */
    public static final String ORDER_NOTIFY_DLX_QUEUE = "order.notify.dlx.queue";
    /** 订单交换机（主交换机） */
    public static final String ORDER_EXCHANGE = "order.exchange";
    /** 死信交换机 */
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    /** 订单通知路由键 */
    public static final String ORDER_NOTIFY_ROUTING_KEY = "order.notify";

    /**
     * 订单通知队列（含死信配置）
     * - deadLetterExchange: 消息成为死信后转发到死信交换机
     * - ttl: 消息存活时间 30 秒
     * - maxLength: 队列最大消息数 1000，防止积压
     */
    @Bean
    public Queue orderNotifyQueue() {
        return QueueBuilder.durable(ORDER_NOTIFY_QUEUE)
                .deadLetterExchange(ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey(ORDER_NOTIFY_ROUTING_KEY)
                .ttl(30000)
                .maxLength(1000)
                .build();
    }

    /** 死信队列（接收超时或被拒绝的消息） */
    @Bean
    public Queue orderNotifyDlxQueue() {
        return QueueBuilder.durable(ORDER_NOTIFY_DLX_QUEUE).build();
    }

    /** 订单交换机（Direct 类型，持久化） */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    /** 死信交换机（Direct 类型，持久化） */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    /** 绑定：主队列 → 主交换机（路由键 order.notify） */
    @Bean
    public Binding orderNotifyBinding() {
        return BindingBuilder.bind(orderNotifyQueue())
                .to(orderExchange())
                .with(ORDER_NOTIFY_ROUTING_KEY);
    }

    /** 绑定：死信队列 → 死信交换机（路由键 order.notify） */
    @Bean
    public Binding orderDlxBinding() {
        return BindingBuilder.bind(orderNotifyDlxQueue())
                .to(orderDlxExchange())
                .with(ORDER_NOTIFY_ROUTING_KEY);
    }
}
