package com.developer.group.dto;

import lombok.Data;

@Data
public class GroupMemberDTO {

    private Long userId;

    private String aliasName;

    private String headImage;

    private Boolean quit;

    private Boolean online;

    private String remark;


}
