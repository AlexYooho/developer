package com.developer.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum WalletStatusEnum {

    NORMAL(0,"正常"),
    FROZEN(1,"冻结");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static WalletStatusEnum fromCode(Integer code){
        for (WalletStatusEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
