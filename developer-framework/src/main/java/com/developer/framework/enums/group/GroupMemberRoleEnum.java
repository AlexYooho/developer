package com.developer.framework.enums.group;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupMemberRoleEnum {

    MEMBER(0,"成员"),

    ADMIN(1,"管理员"),

    OWNER(2,"群主");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

}
