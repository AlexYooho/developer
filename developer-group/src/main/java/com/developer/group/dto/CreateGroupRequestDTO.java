package com.developer.group.dto;

import lombok.Data;

@Data
public class CreateGroupRequestDTO {

    private Long id;

    private String name;

    private Long ownerId;

    private String headImage;

    private String headImageThumb;

    private String notice;

    private String aliasName;

    private String remark;

}