package com.developer.framework.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.developer.framework.model.DeveloperResult;

public interface IMessageProcessor {

    RabbitMQEventTypeEnum eventType();

    DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto);


}
