package com.developer.message.dto;

import lombok.Data;

@Data
public class RemoveMessageRequestDTO {

    private String serialNo;

    private Long targetId;

}
