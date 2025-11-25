package com.developer.message.service;

import com.developer.framework.enums.message.MessageContentTypeEnum;

public interface CommonMessageService {

    /**
     * 是否支付类型消息
     * @param messageContentTypeEnum
     * @return
     */
    Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum);

}
