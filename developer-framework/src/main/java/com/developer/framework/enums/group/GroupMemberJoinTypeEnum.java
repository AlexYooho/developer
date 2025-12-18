package com.developer.framework.enums.group;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupMemberJoinTypeEnum {

    INVITE(0,"邀请"),

    SEARCH(1,"搜索");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }
}
