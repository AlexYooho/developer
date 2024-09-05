package com.developer.friend.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageMainType {

    PRIVATE_MESSAGE(0,"私聊消息"),

    GROUP_MESSAGE(1,"群聊消息"),

    SYSTEM_MESSAGE(2,"系统消息"),

    SUBSCRIBE_MESSAGE(3,"订阅消息");

    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }
}
