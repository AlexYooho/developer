package com.developer.im.netty.protocol.codec;

import com.developer.im.model.IMMessageBodyModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class WSEncoder extends MessageToMessageEncoder<IMMessageBodyModel> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IMMessageBodyModel sendMessageInfo, List<Object> list) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(sendMessageInfo);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(content);
        list.add(textWebSocketFrame);
    }
}
