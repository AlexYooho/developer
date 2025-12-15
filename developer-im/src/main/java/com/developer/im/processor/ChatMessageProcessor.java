package com.developer.im.processor;

import com.developer.framework.model.DeveloperResult;
import com.developer.im.dto.ChatMessageBodyDTO;
import com.developer.im.model.IMMessageBodyModel;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatMessageProcessor extends AbstractMessageProcessor<ChatMessageBodyDTO>{

    @Override
    public DeveloperResult<Boolean> handler(ChatMessageBodyDTO messageBody) {
        try {
            // 找到对应target的ChannelCtx
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(messageBody.getBodyItem().getTargetUserInfo().getUserId(), messageBody.getBodyItem().getTargetUserInfo().getTerminal().code());
            if (channelCtx != null) {
                IMMessageBodyModel sendMessageInfo = new IMMessageBodyModel();
                sendMessageInfo.setCmd(messageBody.getCmd());
                sendMessageInfo.setData(messageBody.getBodyItem());
                channelCtx.channel().writeAndFlush(sendMessageInfo);
            } else {
                return DeveloperResult.error(messageBody.getBodyItem().getSerialNo(),"未找到channel");
            }
        }catch (Exception e){
            return DeveloperResult.error(messageBody.getBodyItem().getSerialNo(),"发送消息异常");
        }
        return DeveloperResult.success(messageBody.getBodyItem().getSerialNo());
    }
}
