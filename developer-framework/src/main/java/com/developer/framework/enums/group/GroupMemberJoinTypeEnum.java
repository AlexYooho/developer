package com.developer.framework.enums.group;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupMemberJoinTypeEnum {

    INVITE(0,"邀请"),

    SEARCH(1,"搜索");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }
}
