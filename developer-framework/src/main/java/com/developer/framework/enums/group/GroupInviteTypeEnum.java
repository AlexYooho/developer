package com.developer.framework.enums.group;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupInviteTypeEnum {

    PASS(0,"直接入群"),

    REVIEW(1,"管理员审核"),

    MUTE(2,"禁止加群");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }


}
