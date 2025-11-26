package com.developer.framework.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;

public interface IMessageProcessor {

    ProcessorTypeEnum processorType();

    DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto);
}
