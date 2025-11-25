package com.developer.framework.enums.payment;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum WalletOperationTypeEnum {

    EXPENDITURE(0,"支出"),

    INCOME(1,"收入");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static WalletOperationTypeEnum fromCode(Integer code){
        for (WalletOperationTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

}
