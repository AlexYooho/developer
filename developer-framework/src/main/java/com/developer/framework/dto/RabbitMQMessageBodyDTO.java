package com.developer.framework.dto;

import com.alibaba.fastjson.JSON;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class RabbitMQMessageBodyDTO implements Serializable {

    public String serialNo;

    public String type;

    public Object data;

    public String token;

    public RabbitMQEventTypeEnum messageType;

    public <T> T parseData(Class<T> clazz){
        if(data==null || "".equals(data)){
            return null;
        }

        return JSON.parseObject(data.toString(), clazz);
    }
}
