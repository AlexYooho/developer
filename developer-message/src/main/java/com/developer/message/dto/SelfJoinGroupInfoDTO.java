package com.developer.message.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SelfJoinGroupInfoDTO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 群id
     */
    private Long groupId;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 群头像
     */
    private String groupHeadImage;

    /**
     * 是否在群
     */
    private Boolean quit;

    /**
     * 群昵称
     */
    private String aliasName;

    /**
     * 创建时间
     */
    private Date createdTime;
}
