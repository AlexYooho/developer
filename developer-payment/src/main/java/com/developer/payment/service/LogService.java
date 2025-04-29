package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;

public interface LogService {

    DeveloperResult<Boolean> modifyRedPacketMessageStatus(String serialNo,Integer sendStatus);

}
