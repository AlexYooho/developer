package com.developer.framework.enums.payment;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TransferStatusEnum {
    PENDING(0,"等待处理"),

    SUCCESS(1,"成功"),

    FAILED(2,"失败"),

    REFUND(3,"退回");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static TransferStatusEnum fromCode(Integer code){
        for (TransferStatusEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }


}
