package com.developer.group.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class BatchModifyGroupMemberInfoRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("list")
    private List<SelfJoinGroupInfoDTO> list;

}
