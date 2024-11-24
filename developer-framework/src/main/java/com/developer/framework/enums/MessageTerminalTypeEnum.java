package com.developer.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum MessageTerminalTypeEnum {

    WEB(0,"web"),
    APP(1,"app");

    @EnumValue
    private Integer code;

    private String desc;


    public static MessageTerminalTypeEnum fromCode(Integer code){
        for (MessageTerminalTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static List<Integer> codes(){
        return Arrays.stream(values()).map(MessageTerminalTypeEnum::code).collect(Collectors.toList());
    }

    public Integer code(){
        return this.code;
    }

    public String desc(){
        return this.desc;
    }

}
