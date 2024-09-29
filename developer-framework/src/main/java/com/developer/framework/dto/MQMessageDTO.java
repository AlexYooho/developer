package com.developer.framework.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MQMessageDTO<T> implements Serializable {

    public String serialNo;

    public String type;

    public T data;
}
