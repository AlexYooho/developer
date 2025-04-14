package com.developer.im.processor;

import com.developer.framework.model.DeveloperResult;
import com.developer.im.enums.IMCmdType;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractMessageProcessor<T> {

    public void handler(ChannelHandlerContext ctx,T data){}

    public abstract DeveloperResult<Boolean> handler(T data, IMCmdType cmdType);

    public T trans(Object object){
        return (T) object;
    }


}
