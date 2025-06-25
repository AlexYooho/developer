package com.developer.message.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessageDTO extends SendMessageResultDTO {

    private Long receiverId;

}
