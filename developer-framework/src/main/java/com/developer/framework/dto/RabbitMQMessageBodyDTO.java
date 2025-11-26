package com.developer.framework.dto;

import com.alibaba.fastjson.JSON;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMQMessageBodyDTO implements Serializable {

    public String serialNo;

    public String type;

    public Object data;

    public String token;

    public ProcessorTypeEnum processorType;

    public <T> T parseData(Class<T> clazz){
        if(data==null || "".equals(data)){
            return null;
        }

        return JSON.parseObject(data.toString(), clazz);
    }
}
