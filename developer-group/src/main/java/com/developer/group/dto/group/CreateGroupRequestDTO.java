package com.developer.group.dto.group;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class CreateGroupRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    /*
    群id
     */
    @JsonProperty("id")
    private Long id;

    /*
    群名称
     */
    @JsonProperty("group_name")
    private String groupName;

    /*
    群成员id
     */
    @JsonProperty("member_user_ids")
    private List<Long> memberUserIds;

}
