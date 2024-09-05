package com.developer.framework.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum IMTerminalTypeEnum {

    WEB(0,"web"),
    APP(1,"app");

    private Integer code;

    private String desc;


    public static IMTerminalTypeEnum fromCode(Integer code){
        for (IMTerminalTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static List<Integer> codes(){
        return Arrays.stream(values()).map(IMTerminalTypeEnum::code).collect(Collectors.toList());
    }

    public Integer code(){
        return this.code;
    }

}
