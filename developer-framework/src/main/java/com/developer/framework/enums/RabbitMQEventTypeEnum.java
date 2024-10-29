package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RabbitMQEventTypeEnum {

    PAYMENT(0,"PAYMENT"),

    IM(1,"IM"),

    MESSAGE_LIKE(2,"MESSAGE_LIKE");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }
}
