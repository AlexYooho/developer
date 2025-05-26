package com.developer.im.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMProcessorBaseModel {

    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 指令类型
     */
    @JsonProperty("cmd")
    private Integer cmd;

}
