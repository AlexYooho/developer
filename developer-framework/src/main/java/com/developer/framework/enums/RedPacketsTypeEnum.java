package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RedPacketsTypeEnum {

    NORMAL(0,"普通红包"),

    LUCKY(1,"拼手气红包");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static RedPacketsTypeEnum fromCode(Integer code){
        for (RedPacketsTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
