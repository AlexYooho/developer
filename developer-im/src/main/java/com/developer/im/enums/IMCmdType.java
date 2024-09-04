package com.developer.im.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMCmdType {

    LOGIN(0,"登陆"),

    HEART_BEAT(1,"心跳"),

    FORCE_LOGUT(2,"强制下线"),

    PRIVATE_MESSAGE(3,"私聊消息"),

    GROUP_MESSAGE(4,"群发消息"),

    SYSTEM_MESSAGE(5,"系统消息"),

    SUBSCRIBE_MESSAGE(6,"订阅消息");


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
