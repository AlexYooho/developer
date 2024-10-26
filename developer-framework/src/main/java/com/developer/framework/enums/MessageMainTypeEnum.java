package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageMainTypeEnum {

    PRIVATE_MESSAGE(0,"私聊消息"),

    GROUP_MESSAGE(1,"群聊消息"),

    SYSTEM_MESSAGE(2,"系统消息"),

    SUBSCRIBE_MESSAGE(3,"订阅消息");

    @EnumValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public static MessageMainTypeEnum fromCode(Integer code){
        for (MessageMainTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
