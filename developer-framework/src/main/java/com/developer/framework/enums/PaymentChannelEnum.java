package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentChannelEnum{

    FRIEND(0,"好友"),

    GROUP(1,"群组"),

    SCAN_CODE(2,"扫码");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static PaymentChannelEnum fromCode(Integer code){
        for (PaymentChannelEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
