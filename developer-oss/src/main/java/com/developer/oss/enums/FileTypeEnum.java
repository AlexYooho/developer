package com.developer.oss.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FileTypeEnum {

    FILE(0,"file"),
    IMAGE(1,"image"),
    VIDEO(2,"video"),
    AUDIO(3,"audio");


    @EnumValue
    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public String desc() {return this.desc;}

}
