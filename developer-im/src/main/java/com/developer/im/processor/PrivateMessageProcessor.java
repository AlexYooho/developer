package com.developer.im.processor;

import com.developer.im.constant.RedisKeyConstant;
import com.developer.im.enums.IMCmdType;
import com.developer.im.enums.SendCodeType;
import com.developer.im.model.IMRecvInfoModel;
import com.developer.im.model.IMSendMessageInfoModel;
import com.developer.im.model.IMUserInfoModel;
import com.developer.im.model.SendResultModel;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrivateMessageProcessor extends AbstractMessageProcessor<IMRecvInfoModel> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void handler(IMRecvInfoModel receiveMessageInfo, IMCmdType cmdType) {
        IMUserInfoModel sender = receiveMessageInfo.getSender();
        IMUserInfoModel receiver = receiveMessageInfo.getReceivers().get(0);
        log.info("接收消息,发送者:{},接收者:{},消息内容:{}",sender.getId(),receiver.getId(),receiveMessageInfo.getData());
        try{
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal().code());
            if(channelCtx!=null){
                IMSendMessageInfoModel sendMessageInfo = new IMSendMessageInfoModel();
                sendMessageInfo.setCmd(cmdType.code());
                sendMessageInfo.setData(receiveMessageInfo.getData());
                channelCtx.channel().writeAndFlush(sendMessageInfo);
                sendResult(receiveMessageInfo,SendCodeType.SUCCESS);
            }else{
                sendResult(receiveMessageInfo,SendCodeType.NOT_FIND_CHANNEL);
                log.info("未找到channel,发送者:{},接收者:{},消息内容:{}",sender.getId(),receiver.getId(),receiveMessageInfo.getData());
            }
        }catch (Exception ex){
            sendResult(receiveMessageInfo,SendCodeType.UNKONW_ERROR);
            log.info("发送异常,发送者:{},接收者:{},消息内容:{}",sender.getId(),receiver.getId(),receiveMessageInfo.getData());
        }
    }

    /**
     * 发送结果
     * @param receiveMessageInfo
     * @param sendCode
     */
    private void sendResult(IMRecvInfoModel receiveMessageInfo, SendCodeType sendCode){
        if(receiveMessageInfo.getSendResult()){
            SendResultModel sendResult = new SendResultModel();
            sendResult.setSender(receiveMessageInfo.getSender());
            sendResult.setReceiver(receiveMessageInfo.getReceivers().get(0));
            sendResult.setCode(sendCode);
            sendResult.setData(receiveMessageInfo.getData());
            String key = RedisKeyConstant.IM_RESULT_PRIVATE_QUEUE;
            redisTemplate.opsForList().rightPush(key, sendResult);
        }
    }

}
