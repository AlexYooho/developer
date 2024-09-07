package com.developer.im.service;

import com.developer.framework.dto.MessageDTO;

public abstract class AbstractMessageTypeService {

    public abstract void handler(MessageDTO messageDTO);

}
