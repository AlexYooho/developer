package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RabbitMQEventTypeEnum {

    PAYMENT(0,"PAYMENT"),

    IM(1,"IM"),

    MESSAGE_LIKE(2,"MESSAGE_LIKE"),

    MESSAGE(3,"MESSAGE"),

    RED_PACKETS_RECOVERY(4,"RED_PACKETS_RECOVERY");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }
}
