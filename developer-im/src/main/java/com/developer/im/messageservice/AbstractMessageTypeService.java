package com.developer.im.messageservice;

import com.developer.im.dto.MessageDTO;
import com.developer.im.enums.MessageMainType;

public abstract class AbstractMessageTypeService {

    public abstract MessageMainType messageMainType();

    public abstract void handler(MessageDTO messageDTO);

}
