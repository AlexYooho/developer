package com.developer.framework.enums.friend;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AddFriendChannelEnum {
    UNKNOWN(0,"未知"),
    SEARCH(1,"搜索"),
    GROUP(2,"群聊"),
    SHARE(3,"推荐");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public String desc() {return this.desc;}
}

