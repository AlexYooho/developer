package com.developer.im.processor;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.im.dto.PushMessageBodyDTO;
import com.developer.im.enums.SendCodeType;
import com.developer.im.model.IMMessageBodyModel;
import com.developer.im.model.IMUserInfoModel;
import com.developer.im.model.SendResultModel;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class IMChatMessageProcessor extends AbstractMessageProcessor<PushMessageBodyDTO>{

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public DeveloperResult<Boolean> handler(PushMessageBodyDTO messageBody) {
        List<IMUserInfoModel> receivers = messageBody.getMessageReceiverList();
        for (IMUserInfoModel receiver : receivers) {
            try {
                ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getSenderId(), receiver.getTerminal().code());
                if (channelCtx != null) {
                    IMMessageBodyModel sendMessageInfo = new IMMessageBodyModel();
                    sendMessageInfo.setCmd(messageBody.getCmd());
                    sendMessageInfo.setData(messageBody);
                    channelCtx.channel().writeAndFlush(sendMessageInfo);
                    sendResult(messageBody, receiver, SendCodeType.SUCCESS);
                } else {
                    sendResult(messageBody, receiver, SendCodeType.NOT_FIND_CHANNEL);
                    return DeveloperResult.error(messageBody.getSerialNo(),"未找到channel");
                }
            }catch (Exception e){
                sendResult(messageBody, receiver, SendCodeType.UNKONW_ERROR);
                return DeveloperResult.error(messageBody.getSerialNo(),"发送消息异常");
            }
        }
        return DeveloperResult.success(messageBody.getSerialNo());
    }

    /**
     * 发送结果
     * @param messageBody
     * @param receiver
     * @param sendCode
     */
    private void sendResult(PushMessageBodyDTO messageBody, IMUserInfoModel receiver, SendCodeType sendCode){
        if(messageBody.getSendResult()){
            SendResultModel sendResult = new SendResultModel();
            sendResult.setSender(receiver);
            sendResult.setReceiver(receiver);
            sendResult.setCode(sendCode);
            sendResult.setData(messageBody.getData());
            String key = RedisKeyConstant.IM_RESULT_GROUP_QUEUE;
            redisUtil.lrPush(key, sendResult);
        }
    }
}
