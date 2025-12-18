package com.developer.framework.enums.payment;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RedPacketsStatusEnum {

    PENDING(0,"等待处理"),

    FINISHED(1,"领取完"),

    EXPIRED(2,"过期"),

    REFUND(3,"退回"),

    SEND_FAILURE(4,"发送失败");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static RedPacketsStatusEnum fromCode(Integer code){
        for (RedPacketsStatusEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
