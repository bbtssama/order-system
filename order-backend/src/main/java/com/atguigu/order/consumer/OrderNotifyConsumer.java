package com.atguigu.order.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单通知消息消费者
 * <p>
 * 监听 RabbitMQ 的 order.notify.queue 队列，异步处理订单通知。
 * 当前实现为模拟发送通知（短信/邮件/站内信），生产环境可对接真实通知服务。
 * 消息发送失败不会影响订单创建（由 OrderServiceImpl 中的 try-catch 兜底）。
 * </p>
 */
@Component
public class OrderNotifyConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderNotifyConsumer.class);

    /**
     * 处理订单通知消息
     *
     * @param message JSON 格式的订单通知消息，包含 orderId、orderNo、userId、totalAmount
     */
    @RabbitListener(queues = "order.notify.queue")
    public void handleOrderNotify(String message) {
        log.info("====== 收到订单通知消息 ======");
        log.info("消息内容: {}", message);
        log.info("模拟发送通知：短信/邮件/站内信...");
        log.info("====== 订单通知处理完成 ======");
    }
}
