package com.developer.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessageDTO extends SendMessageResultDTO {

    @JsonProperty("receiver_id")
    private Long receiverId;

//    private Long receiverId;

}
