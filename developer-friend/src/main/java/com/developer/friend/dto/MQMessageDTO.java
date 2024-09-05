package com.developer.friend.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MQMessageDTO<T> implements Serializable {

    public String serialNo;

    public String type;

    public T data;
}
