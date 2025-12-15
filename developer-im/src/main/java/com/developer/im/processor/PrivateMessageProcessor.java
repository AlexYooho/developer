package com.developer.im.processor;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.enums.SendCodeType;
import com.developer.im.model.*;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrivateMessageProcessor extends AbstractMessageProcessor<IMChatPrivateMessageModel> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public DeveloperResult<Boolean> handler(IMChatPrivateMessageModel messageBody) {
        IMUserInfoModel sender = messageBody.getSender();
        IMUserInfoModel receiver = messageBody.getMessageReceiver();
        log.info("接收消息,发送者:{},接收者:{},消息内容:{}",sender.getUserId(),receiver.getUserId(),messageBody.getData());
        try{
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getUserId(), receiver.getTerminal().code());
            if(channelCtx!=null){
                IMMessageBodyModel sendMessageInfo = new IMMessageBodyModel();
                sendMessageInfo.setCmd(messageBody.getCmd());
                sendMessageInfo.setData(messageBody);
                channelCtx.channel().writeAndFlush(sendMessageInfo);
                sendResult(messageBody,SendCodeType.SUCCESS);
            }else{
                sendResult(messageBody,SendCodeType.NOT_FIND_CHANNEL);
                log.info("未找到channel,发送者:{},接收者:{},消息内容:{}",sender.getUserId(),receiver.getUserId(),messageBody.getData());
                return DeveloperResult.error(messageBody.getSerialNo(),"未找到channel");
            }
        }catch (Exception ex){
            sendResult(messageBody,SendCodeType.UNKONW_ERROR);
            log.info("发送异常,发送者:{},接收者:{},消息内容:{}",sender.getUserId(),receiver.getUserId(),messageBody.getData());
            return DeveloperResult.error(messageBody.getSerialNo(),"发送异常");
        }
        return DeveloperResult.success(messageBody.getSerialNo());
    }

    /**
     * 发送结果
     * @param receiveMessageInfo
     * @param sendCode
     */
    private void sendResult(IMChatPrivateMessageModel receiveMessageInfo, SendCodeType sendCode){
        if(receiveMessageInfo.getSendResult()){
            SendResultModel sendResult = new SendResultModel();
            sendResult.setSender(receiveMessageInfo.getSender());
            sendResult.setReceiver(receiveMessageInfo.getMessageReceiver());
            sendResult.setCode(sendCode);
            sendResult.setData(receiveMessageInfo.getData());
            String key = RedisKeyConstant.IM_RESULT_PRIVATE_QUEUE;
            redisTemplate.opsForList().rightPush(key, sendResult);
        }
    }

}
