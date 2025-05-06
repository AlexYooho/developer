package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProcessorTypeEnum {

    PAYMENT(0,"支付"),

    IM(1,"IM即时消息"),

    MESSAGE_LIKE(2,"消息点赞"),

    MESSAGE(3,"消息"),

    TRANSACTION_EXPIRED_CHECK(4,"交易过期检查"),

    TRANSACTION_MESSAGE_SEND_CHECK(5,"交易消息发送检测");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }
}
