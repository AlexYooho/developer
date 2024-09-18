package com.developer.framework.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageContentTypeEnum {

    TEXT(0,"文本"),

    IMAGE(1,"图片"),

    DOCUMENT(2,"文档");

    private final Integer code;

    private final String desc;

    public static MessageContentTypeEnum fromCode(Integer code){
        for (MessageContentTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
    public Integer code(){
        return this.code;
    }

}