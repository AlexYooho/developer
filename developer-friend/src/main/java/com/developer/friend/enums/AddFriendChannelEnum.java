package com.developer.friend.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AddFriendChannelEnum {
    SEARCH(0,"搜索"),
    SHARE(1,"分享"),
    GROUP(2,"群添加");

    @EnumValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public String desc() {return this.desc;}
}

