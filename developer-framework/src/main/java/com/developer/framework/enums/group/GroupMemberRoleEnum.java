package com.developer.framework.enums.group;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupMemberRoleEnum {

    MEMBER(0,"成员"),

    ADMIN(1,"管理员"),

    OWNER(2,"群主");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

}
