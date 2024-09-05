package com.developer.im.messageservice;

import com.developer.framework.dto.MessageDTO;
import com.developer.framework.enums.MessageMainTypeEnum;

public abstract class AbstractMessageTypeService {

    public abstract MessageMainTypeEnum messageMainType();

    public abstract void handler(MessageDTO messageDTO);

}
