package com.developer.payment.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TransactionTypeEnum {

    TRANSFER(0,"转账"),

    RECHARGE(1,"充值"),

    WITHDRAW(2,"提现"),

    RED_PACKET(3,"红包");

    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static TransactionTypeEnum fromCode(Integer code){
        for (TransactionTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

}
