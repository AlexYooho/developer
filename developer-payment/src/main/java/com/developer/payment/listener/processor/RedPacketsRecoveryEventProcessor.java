package com.developer.payment.listener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import org.springframework.stereotype.Component;

@Component
public class RedPacketsRecoveryEventProcessor implements IMessageProcessor {
    @Override
    public RabbitMQEventTypeEnum eventType() {
        return RabbitMQEventTypeEnum.RED_PACKETS_RECOVERY;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        return null;
    }
}
