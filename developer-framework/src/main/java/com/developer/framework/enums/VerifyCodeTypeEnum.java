package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VerifyCodeTypeEnum {

    REGISTER_ACCOUNT(0,"注册账号"),

    MODIFY_PASSWORD(1,"修改密码");

    @EnumValue
    private final Integer code;

    private final String desc;

    public Integer code(){
        return this.code;
    }

    public static VerifyCodeTypeEnum fromCode(Integer code){
        for (VerifyCodeTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

}
