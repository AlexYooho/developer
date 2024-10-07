package com.developer.im.processor;

import cn.hutool.core.collection.CollectionUtil;
import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMGroupMessageModel;
import com.developer.im.model.IMPrivateMessageModel;
import com.developer.im.model.IMRecvInfoModel;
import com.developer.im.model.IMUserInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IMProcessor {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 发送私聊消息
     * @param message
     * @param <T>
     */
    public<T> void sendPrivateMessage(IMPrivateMessageModel<T> message, IMCmdType cmdType){
        for (Integer terminal:message.getRecvTerminals()){
            // 获取对方连接的channelId
            String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, message.getReceiverId().toString(), terminal.toString());
            Integer serverId = (Integer)redisTemplate.opsForValue().get(key);

            if(serverId==null || serverId<=0){
                continue;
            }

            // 如果对方在线
            String sendKey = String.join(":", RedisKeyConstant.IM_MESSAGE_PRIVATE_QUEUE, serverId.toString());
            IMRecvInfoModel recvInfo = new IMRecvInfoModel();
            recvInfo.setCmd(cmdType.code());
            recvInfo.setSendResult(message.getSendResult());
            recvInfo.setSender(message.getSender());
            recvInfo.setReceivers(Collections.singletonList(new IMUserInfoModel(message.getReceiverId(), MessageTerminalTypeEnum.fromCode(terminal))));
            recvInfo.setData(message.getData());
            AbstractMessageProcessor processor = ProcessorFactory.getHandler(cmdType);
            processor.handler(recvInfo,cmdType);
        }
    }

    /**
     * 发送群消息
     * @param message
     * @param <T>
     */
    public<T> void sendGroupMessage(IMGroupMessageModel<T> message){
        // 根据群聊每个成员所连的IM-server，进行分组
        HashMap<String, IMUserInfoModel> sendMap = new HashMap<>();
        for (Integer terminal : message.getRecvTerminals()) {
            message.getRecvIds().stream().forEach(id->{
                String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                sendMap.put(key,new IMUserInfoModel(id, MessageTerminalTypeEnum.fromCode(terminal)));
            });
        }

        // 批量拉取
        List<Object> serverIds = redisTemplate.opsForValue().multiGet(sendMap.keySet());
        // 格式:map<服务器id,list<接收方>>
        Map<Integer, List<IMUserInfoModel>> serverMap = new HashMap<>();
        List<IMUserInfoModel> offLineUsers = new LinkedList<>();
        int idx = 0;
        for (Map.Entry<String,IMUserInfoModel> entry : sendMap.entrySet()) {
            Integer serverId = (Integer)serverIds.get(idx++);
            if (serverId != null) {
                List<IMUserInfoModel> list = serverMap.computeIfAbsent(serverId, o -> new LinkedList<>());
                list.add(entry.getValue());
            } else {
                // 加入离线列表
                offLineUsers.add(entry.getValue());
            }
        };
        // 逐个server发送
        for (Map.Entry<Integer, List<IMUserInfoModel>> entry : serverMap.entrySet()) {
            IMRecvInfoModel recvInfo = new IMRecvInfoModel();
            recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
            recvInfo.setReceivers(new LinkedList<>(entry.getValue()));
            recvInfo.setSender(message.getSender());
            recvInfo.setSendResult(message.getSendResult());
            recvInfo.setData(message.getData());
            AbstractMessageProcessor processor = ProcessorFactory.getHandler(IMCmdType.GROUP_MESSAGE);
            processor.handler(recvInfo,IMCmdType.GROUP_MESSAGE);
        }
    }

    /**
     * 获取在线终端
     * @param userIds
     * @return
     */
    public Map<Long,List<MessageTerminalTypeEnum>> getOnlineTerminal(List<Long> userIds){
        if(CollectionUtil.isEmpty(userIds)){
            return Collections.EMPTY_MAP;
        }
        // 把所有用户的key都存起来
        Map<String, IMUserInfoModel> userMap = new HashMap<>();
        for(Long id:userIds){
            for (Integer terminal : MessageTerminalTypeEnum.codes()) {
                String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                userMap.put(key,new IMUserInfoModel(id, MessageTerminalTypeEnum.fromCode(terminal)));
            }
        }
        // 批量拉取
        List<Object> serverIds = redisTemplate.opsForValue().multiGet(userMap.keySet());
        int idx = 0;
        Map<Long,List<MessageTerminalTypeEnum>> onlineMap = new HashMap<>();
        for (Map.Entry<String,IMUserInfoModel> entry : userMap.entrySet()) {
            // serverid有值表示用户在线
            if(serverIds.get(idx++) != null){
                IMUserInfoModel userInfo = entry.getValue();
                List<MessageTerminalTypeEnum> terminals = onlineMap.computeIfAbsent(userInfo.getId(), o -> new LinkedList<>());
                terminals.add(userInfo.getTerminal());
            }
        }
        // 去重并返回
        return onlineMap;
    }

    /**
     * 是否在线
     * @param userId
     * @return
     */
    public Boolean isOnline(Long userId) {
        String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, userId.toString(), "*");
        return !redisTemplate.keys(key).isEmpty();
    }

    /**
     * 在线用户
     * @param userIds
     * @return
     */
    public List<Long> getOnlineUser(List<Long> userIds){
        return new LinkedList<>(getOnlineTerminal(userIds).keySet());
    }
}
