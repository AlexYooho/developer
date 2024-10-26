package com.developer.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TransactionStatusEnum {

    PENDING(0,"等待处理"),

    SUCCESS(1,"成功"),

    FAILED(2,"失败");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static TransactionStatusEnum fromCode(Integer code){
        for (TransactionStatusEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
