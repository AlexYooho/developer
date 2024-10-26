package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentTypeEnum {

    RED_PACKETS(0,"红包"),

    TRANSFER(1,"转账");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static PaymentTypeEnum fromCode(Integer code){
        for (PaymentTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

}
