package com.developer.im.processor;

import com.developer.framework.utils.SpringContext;
import com.developer.im.enums.IMCmdType;

public class ProcessorFactory {

    public static AbstractMessageProcessor getHandler(IMCmdType cmd){
        AbstractMessageProcessor handler = null;
        switch (cmd){
            case LOGIN:
                handler = (AbstractMessageProcessor) SpringContext.getApplicationContext().getBean(LoginProcessor.class);
                break;
            case HEART_BEAT:
                handler = (AbstractMessageProcessor) SpringContext.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE:
            case SYSTEM_MESSAGE:
            case SUBSCRIBE_MESSAGE:
                handler = (AbstractMessageProcessor) SpringContext.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE:
                handler = (AbstractMessageProcessor) SpringContext.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            default:
                break;
        }

        return handler;
    }


}
