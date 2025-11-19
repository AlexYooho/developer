package com.developer.framework.enums.group;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupTypeEnum {

    NORMAL(0,"普通群"),

    BIG(1,"大群");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

}
