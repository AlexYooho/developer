package com.developer.message.dto;

import lombok.Data;

import java.util.List;

@Data
public class ForwardMessageRequestDTO {

    private String serialNo;

    private Long messageId;

    private List<Long> userIdList;

}
