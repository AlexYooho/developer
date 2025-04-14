package com.developer.im.processor;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.model.DeveloperResult;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class GroupMessageProcessor extends AbstractMessageProcessor<IMRecvInfoModel>{

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Async
    @Override
    public DeveloperResult<Boolean> handler(IMRecvInfoModel recvInfo, IMCmdType cmdType) {
        IMUserInfoModel sender = recvInfo.getSender();
        List<IMUserInfoModel> receivers = recvInfo.getReceivers();
        log.info("接收到群消息,发送者:{},接收用户数量:{},内容:{}",sender.getId(),receivers.size(),recvInfo.getData());
        for (IMUserInfoModel receiver : receivers) {
            try {
                ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal().code());
                if (channelCtx != null) {
                    // 推送消息到用户
                    IMSendMessageInfoModel sendMessageInfo = new IMSendMessageInfoModel();
                    sendMessageInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    sendMessageInfo.setData(recvInfo.getData());
                    channelCtx.channel().writeAndFlush(sendMessageInfo);
                    sendResult(recvInfo, receiver, SendCodeType.SUCCESS);
                } else {
                    sendResult(recvInfo, receiver, SendCodeType.NOT_FIND_CHANNEL);
                    log.error("未找到channel,发送者:{},接收id:{},内容:{}", sender.getId(), receiver.getId(), recvInfo.getData());
                    return DeveloperResult.error(recvInfo.getSerialNo(),"未找到channel");
                }
            }catch (Exception e){
                sendResult(recvInfo, receiver, SendCodeType.UNKONW_ERROR);
                log.error("发送群消息异常,发送者:{},接收id:{},内容:{}", sender.getId(), receiver.getId(), recvInfo.getData());
                return DeveloperResult.error(recvInfo.getSerialNo(),"发送群消息异常");
            }
        }
        return DeveloperResult.success(recvInfo.getSerialNo());
    }


    /**
     * 发送结果
     * @param recvInfo
     * @param receiver
     * @param sendCode
     */
    private void sendResult(IMRecvInfoModel recvInfo, IMUserInfoModel receiver, SendCodeType sendCode){
        if(recvInfo.getSendResult()){
            SendResultModel sendResult = new SendResultModel();
            sendResult.setSender(receiver);
            sendResult.setReceiver(receiver);
            sendResult.setCode(sendCode);
            sendResult.setData(recvInfo.getData());
            String key = RedisKeyConstant.IM_RESULT_GROUP_QUEUE;
            redisTemplate.opsForList().rightPush(key, sendResult);
        }
    }
}
