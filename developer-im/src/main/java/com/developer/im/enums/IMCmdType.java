package com.developer.im.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMCmdType {

    LOGIN(0,"登陆"),

    HEART_BEAT(1,"心跳"),

    FORCE_LOGOUT(2,"强制下线"),

    PRIVATE_MESSAGE(3,"私聊消息"),

    GROUP_MESSAGE(4,"群聊消息"),

    SYSTEM_MESSAGE(5,"系统消息"),

    SUBSCRIBE_MESSAGE(6,"订阅消息"),

    LOGOUT(7,"离线"),

    CHAT_MESSAGE(8,"聊天消息");

    @EnumValue
    @JsonValue
    private Integer code;

    private String desc;


    public static IMCmdType transCode(Integer code){
        for (IMCmdType typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }


    public Integer code(){
        return this.code;
    }

}
