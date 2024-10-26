package com.developer.friend.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

/**
 * 添加好友状态
 */
@AllArgsConstructor
public enum AddFriendStatusEnum {
    SENT(0,"已发送"),
    VIEWED(1,"已查看"),
    AGREED(2,"已同意"),
    REJECTED(3,"已拒绝");

    @EnumValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public String desc() {return this.desc;}
}

