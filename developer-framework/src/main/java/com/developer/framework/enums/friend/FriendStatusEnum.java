package com.developer.framework.enums.friend;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FriendStatusEnum {
    NORMAL(0,"正常"),
    DELETE(1,"删除"),
    BLOCKED(2,"拉黑");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public String desc() {return this.desc;}
}
