package com.developer.message.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModifyRedPacketsMessageStatusRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("message_status")
    private Integer messageStatus;

}
