package com.developer.payment.listener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import org.springframework.stereotype.Component;

@Component
public class RedPacketsReturnProcessor implements IMessageProcessor {
    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.RED_PACKETS_RETURN;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        return null;
    }
}
