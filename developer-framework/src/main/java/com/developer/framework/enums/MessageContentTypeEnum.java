package com.developer.framework.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageContentTypeEnum {

    TEXT(0,"文本"),

    IMAGE(1,"图片"),

    DOCUMENT(2,"文档");

    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }

}