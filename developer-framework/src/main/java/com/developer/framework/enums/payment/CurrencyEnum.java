package com.developer.framework.enums.payment;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CurrencyEnum {

    CNY(0,"人民币");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public static CurrencyEnum fromCode(Integer code){
        for (CurrencyEnum typeEnum:values()) {
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
