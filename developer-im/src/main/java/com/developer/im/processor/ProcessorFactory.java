package com.developer.im.processor;

import com.developer.framework.utils.SpringContext;
import com.developer.im.enums.IMCmdType;

public class ProcessorFactory {

    public static AbstractMessageProcessor getHandler(IMCmdType cmd){
        AbstractMessageProcessor handler = null;
        switch (cmd){
            case LOGIN: // 登录
                handler = SpringContext.getApplicationContext().getBean(LoginProcessor.class);
                break;
            case LOGOUT: // 登出
                handler = SpringContext.getApplicationContext().getBean(LogoutProcessor.class);
                break;
            case HEART_BEAT: // 心跳
                handler = SpringContext.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE: // 消息
            case SYSTEM_MESSAGE:
            case SUBSCRIBE_MESSAGE:
                handler = SpringContext.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE: // 群消息
                handler = SpringContext.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            case CHAT_MESSAGE:
                handler = SpringContext.getApplicationContext().getBean(ChatMessageProcessor.class);
                break;
            default:
                break;
        }

        return handler;
    }


}
