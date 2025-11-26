package com.developer.framework.enums.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum TerminalTypeEnum {

    WEB(0,"web"),
    ANDROID(1,"app"),
    IOS(2,"ios");

    @EnumValue
    private Integer code;

    private String desc;


    public static TerminalTypeEnum fromCode(Integer code){
        for (TerminalTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static List<Integer> codes(){
        return Arrays.stream(values()).map(TerminalTypeEnum::code).collect(Collectors.toList());
    }

    public Integer code(){
        return this.code;
    }

    public String desc(){
        return this.desc;
    }

}
