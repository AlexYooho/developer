package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class ForwardMessageRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("user_id_list")
    private List<Long> userIdList;

}
