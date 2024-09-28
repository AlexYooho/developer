package com.developer.message.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageLikeEventDTO {

    private Long userId;

    private Long messageId;

}
