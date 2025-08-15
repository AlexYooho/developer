package com.developer.im.processor;

import cn.hutool.core.bean.BeanUtil;
import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.constant.ChannelAttrKey;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMHeartbeatInfoModel;
import com.developer.im.model.IMMessageBodyModel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HeartbeatProcessor extends AbstractMessageProcessor<IMHeartbeatInfoModel> {


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, IMHeartbeatInfoModel data) {
        // 响应ws
        IMMessageBodyModel sendMessageInfo = new IMMessageBodyModel();
        sendMessageInfo.setCmd(IMCmdType.HEART_BEAT);
        ctx.channel().writeAndFlush(sendMessageInfo);

        // 设置属性
        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf(ChannelAttrKey.HEARTBEAT_TIMES);
        Long heartbeatTimes = ctx.channel().attr(heartBeatAttr).get();
        ctx.channel().attr(heartBeatAttr).set(++heartbeatTimes);
        if (heartbeatTimes % 10 == 0) {
            // 心跳10次
            AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
            Long userId = ctx.channel().attr(userIdAttr).get();
            AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
            Integer terminal = ctx.channel().attr(terminalAttr).get();
            String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID,userId.toString(),terminal.toString());
            redisTemplate.expire(key, DeveloperConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        }
    }

    @Override
    public DeveloperResult<Boolean> handler(IMHeartbeatInfoModel data) {
        return null;
    }

    @Override
    public IMHeartbeatInfoModel trans(Object object) {
        return BeanUtil.fillBeanWithMap((HashMap) object, new IMHeartbeatInfoModel(), false);
    }
}
