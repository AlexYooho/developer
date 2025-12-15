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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class GroupMessageProcessor extends AbstractMessageProcessor<IMChatGroupMessageModel>{

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Async
    @Override
    public DeveloperResult<Boolean> handler(IMChatGroupMessageModel messageBody) {
        IMUserInfoModel sender = messageBody.getSender();
        List<IMUserInfoModel> receivers = messageBody.getMessageReceiverList();
        log.info("接收到群消息,发送者:{},接收用户数量:{},内容:{}",sender.getUserId(),receivers.size(),messageBody.getData());
        for (IMUserInfoModel receiver : receivers) {
            try {
                ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getUserId(), receiver.getTerminal().code());
                if (channelCtx != null) {
                    // 推送消息到用户
                    IMMessageBodyModel sendMessageInfo = new IMMessageBodyModel();
                    sendMessageInfo.setCmd(messageBody.getCmd());
                    sendMessageInfo.setData(messageBody);
                    channelCtx.channel().writeAndFlush(sendMessageInfo);
                    sendResult(messageBody, receiver, SendCodeType.SUCCESS);
                } else {
                    sendResult(messageBody, receiver, SendCodeType.NOT_FIND_CHANNEL);
                    log.error("未找到channel,发送者:{},接收id:{},内容:{}", sender.getUserId(), receiver.getUserId(), messageBody.getData());
                    return DeveloperResult.error(messageBody.getSerialNo(),"未找到channel");
                }
            }catch (Exception e){
                sendResult(messageBody, receiver, SendCodeType.UNKONW_ERROR);
                log.error("发送群消息异常,发送者:{},接收id:{},内容:{}", sender.getUserId(), receiver.getUserId(), messageBody.getData());
                return DeveloperResult.error(messageBody.getSerialNo(),"发送群消息异常");
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
    private void sendResult(IMChatGroupMessageModel messageBody, IMUserInfoModel receiver, SendCodeType sendCode){
        if(messageBody.getSendResult()){
            SendResultModel sendResult = new SendResultModel();
            sendResult.setSender(receiver);
            sendResult.setReceiver(receiver);
            sendResult.setCode(sendCode);
            sendResult.setData(messageBody.getData());
            String key = RedisKeyConstant.IM_RESULT_GROUP_QUEUE;
            redisTemplate.opsForList().rightPush(key, sendResult);
        }
    }
}
