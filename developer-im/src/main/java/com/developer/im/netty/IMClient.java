package com.developer.im.netty;

import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMGroupMessageModel;
import com.developer.im.model.IMPrivateMessageModel;
import com.developer.im.processor.IMProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class IMClient {

    @Autowired
    private IMProcessor processors;

    /**
     * 判断用户是否在线
     *
     * @param userId 用户id
     */
    public Boolean isOnline(Long userId){
        return processors.isOnline(userId);
    }

    /**
     * 判断多个用户是否在线
     *
     * @param userIds 用户id列表
     * @return 在线的用户列表
     */
    public List<Long> getOnlineUser(List<Long> userIds){
        return processors.getOnlineUser(userIds);
    }

    /**
     * 获取用户在线终端类型
     *
     * @param userIds 用户id列表
     * @return 在线的用户终端
     */
    public Map<Long,List<MessageTerminalTypeEnum>> getOnlineTerminal(List<Long> userIds){
        return processors.getOnlineTerminal(userIds);
    }

    /**
     * 发送私聊消息（发送结果通过MessageListener接收）
     *
     * @param message 私有消息
     */
    public<T> void sendPrivateMessage(IMPrivateMessageModel<T> message, IMCmdType cmdType){
        processors.sendPrivateMessage(message,cmdType);
    }

    /**
     * 发送群聊消息（发送结果通过MessageListener接收）
     *
     * @param message 群聊消息
     */
    public<T> void sendGroupMessage(IMGroupMessageModel<T> message){
        processors.sendGroupMessage(message);
    }

}

