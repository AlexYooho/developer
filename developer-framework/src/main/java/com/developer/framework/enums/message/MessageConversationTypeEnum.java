package com.developer.framework.enums.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageConversationTypeEnum {

    PRIVATE_MESSAGE(0,"私聊消息"),

    GROUP_MESSAGE(1,"群聊消息"),

    SYSTEM_MESSAGE(2,"系统消息"),

    SUBSCRIBE_MESSAGE(3,"订阅消息"),

    CHAT_MESSAGE(4,"聊天消息"),;

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public static MessageConversationTypeEnum fromCode(Integer code){
        for (MessageConversationTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
