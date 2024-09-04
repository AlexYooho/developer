package com.developer.im.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SendCodeType {

    SUCCESS(0,"发送成功"),
    NOT_ONLINE(1,"对方当前不在线"),
    NOT_FIND_CHANNEL(2,"未找到对方的channel"),
    UNKONW_ERROR(9999,"未知异常");

    private Integer code;
    private String desc;


    public static SendCodeType fromCode(Integer code){
        for (SendCodeType typeEnum:values()) {
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
