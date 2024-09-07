package com.developer.oss.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FileTypeEnum {

    FILE(0,"file"),
    IMAGE(1,"image"),
    VIDEO(2,"video"),
    AUDIO(3,"audio");



    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

    public String desc() {return this.desc;}

}
