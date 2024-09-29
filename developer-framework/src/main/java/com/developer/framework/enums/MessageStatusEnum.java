package com.developer.framework.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageStatusEnum {

    UNSEND(0,"未送达"),
    SENDED(1,"送达"),
    RECALL(2,"撤回"),
    READED(3,"已读");

    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public static MessageStatusEnum fromCode(Integer code){
        for (MessageStatusEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
