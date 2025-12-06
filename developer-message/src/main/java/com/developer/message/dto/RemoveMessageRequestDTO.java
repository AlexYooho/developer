package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RemoveMessageRequestDTO {

    /*
    消息对象
     */
    @JsonProperty("target_id")
    private Long targetId;

    /*
    所有消息
     */
    @JsonProperty("all")
    private Boolean all = false;

    /*
    消息id
     */
    @JsonProperty("message_id")
    private Long messageId;

}
