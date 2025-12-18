package com.developer.framework.enums.payment;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentChannelEnum {

    FRIEND(0, "好友", true),
    GROUP(1, "群组", true),
    SCAN_CODE(2, "扫码", true);

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    private final boolean supportPaymentChannel;

    public Integer code() {
        return this.code;
    }

    public boolean isSupportType() {
        return this.supportPaymentChannel;
    }

    public static PaymentChannelEnum fromCode(Integer code){
        for (PaymentChannelEnum typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
