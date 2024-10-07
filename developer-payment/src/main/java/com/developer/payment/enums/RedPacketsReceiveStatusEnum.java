package com.developer.payment.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RedPacketsReceiveStatusEnum {

    PENDING(0,"待领取"),

    SUCCESS(1,"成功"),

    FAILED(2,"失败");

    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static RedPacketsReceiveStatusEnum fromCode(Integer code){
        for (RedPacketsReceiveStatusEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

}
