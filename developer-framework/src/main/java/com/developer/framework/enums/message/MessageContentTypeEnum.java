package com.developer.framework.enums.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageContentTypeEnum {

    TEXT(0,"文本"),

    IMAGE(1,"图片"),

    DOCUMENT(2,"文档"),

    RED_PACKETS(3,"红包"),

    TRANSFER(4,"转账"),

    GROUP_INVITE(5,"群邀请");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public static MessageContentTypeEnum fromCode(Integer code){
        for (MessageContentTypeEnum typeEnum:values()) {
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