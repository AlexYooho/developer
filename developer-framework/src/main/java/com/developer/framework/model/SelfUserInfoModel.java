package com.developer.framework.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户信息
 */
@Data
public class SelfUserInfoModel implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 终端类型
     */
    private Integer terminal;

    /**
     * 账户地址
     */
    private String emailAccount;
}
