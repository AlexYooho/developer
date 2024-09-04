package com.developer.im.netty.protocol.codec;

import com.developer.im.model.IMSendMessageInfoModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class WSDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame, List<Object> list) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IMSendMessageInfoModel sendMessageInfo = mapper.readValue(textWebSocketFrame.text(), IMSendMessageInfoModel.class);
        list.add(sendMessageInfo);
    }

}
