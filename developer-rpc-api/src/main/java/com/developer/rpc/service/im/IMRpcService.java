package com.developer.rpc.service.im;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.model.DeveloperResult;

public interface IMRpcService {

    DeveloperResult<Boolean> pushTargetWSNode(RabbitMQMessageBodyDTO dto);

}
