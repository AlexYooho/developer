package com.developer.group.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class CreateGroupRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("owner_id")
    private Long ownerId;

    @JsonProperty("head_image")
    private String headImage;

    @JsonProperty("head_image_thumb")
    private String headImageThumb;

    @JsonProperty("notice")
    private String notice;

    @JsonProperty("alias_name")
    private String aliasName;

    @JsonProperty("remark")
    private String remark;

}
