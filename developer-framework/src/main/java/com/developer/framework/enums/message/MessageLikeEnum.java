package com.developer.framework.enums.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageLikeEnum {

    CANCEL_LIKE(0,"取消点赞"),
    LIKE(1,"点赞");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static MessageLikeEnum fromCode(Integer code){
        for (MessageLikeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
