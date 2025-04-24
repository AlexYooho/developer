package com.developer.framework.constant;

public class DeveloperMQConstant {

    /**
     * rabbitmq 交换机、路由键、队列概念
     * 1、交换机：消息发送方，消息接收方不在同一个应用，需要通过交换机来完成消息的发送和接收。
     * 2、路由键：消息发送方通过路由键来确定消息发送到哪个队列，路由键可以有多个，多个路由键之间用“.”隔开，如“*.message.*”
     * 3、队列：消息接收方，消息接收方需要通过路由键来确定自己需要接收的消息。
     */


    // 交换机
    public static final String MESSAGE_IM_EXCHANGE = "message-im-exchange";

    public static final String MESSAGE_PAYMENT_EXCHANGE = "message-payment-exchange";

    public static final String MESSAGE_CHAT_EXCHANGE = "message-chat-exchange";

    public static final String MESSAGE_DELAY_EXCHANGE = "message-delay-exchange";

    // 路由键
    public static final String MESSAGE_IM_ROUTING_KEY ="message.im";

    public static final String MESSAGE_PAYMENT_ROUTING_KEY = "message.payment";

    public static final String MESSAGE_CHAT_ROUTING_KEY ="message.chat";

    public static final String MESSAGE_DELAY_ROUTING_KEY = "message.delay";

    // 队列
    public static final String MESSAGE_IM_QUEUE="message-im-queue";

    public static final String MESSAGE_PAYMENT_QUEUE = "message-payment-queue";

    public static final String MESSAGE_CHAT_QUEUE="message-chat-queue";

    public static final String MESSAGE_DELAY_QUEUE="message-delay-queue";

}
