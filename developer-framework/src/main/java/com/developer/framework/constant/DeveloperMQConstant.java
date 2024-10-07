package com.developer.framework.constant;

public class DeveloperMQConstant {

    /**
     * rabbitmq 交换机、路由键、队列概念
     * 1、交换机：消息发送方，消息接收方不在同一个应用，需要通过交换机来完成消息的发送和接收。
     * 2、路由键：消息发送方通过路由键来确定消息发送到哪个队列，路由键可以有多个，多个路由键之间用“.”隔开，如“*.message.*”
     * 3、队列：消息接收方，消息接收方需要通过路由键来确定自己需要接收的消息。
     */


    // 交换机
    public static final String MESSAGE_CHAT_EXCHANGE = "message-chat-exchange";

    public static final String MESSAGE_LIKE_EXCHANGE = "message-like-exchange";

    // 路由键
    public static final String CHAT_MESSAGE_ROUTING_KEY ="message.chat";

    public static final String MESSAGE_LIKE_ROUTING_KEY = "message.like";

    public static final String MESSAGE_MONEY_ROUTING_KEY = "message.money";

    // 队列
    public static final String MESSAGE_QUEUE="message-chat-queue";

    public static final String MESSAGE_LIKE_QUEUE = "message-like-queue";

    public static final String MESSAGE_MONEY_QUEUE = "message-money-queue";

}
