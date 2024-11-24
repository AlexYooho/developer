package com.developer.im.processor;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.model.SelfUserInfoModel;
import com.developer.im.config.ResourceServerConfiger;
import com.developer.im.constant.ChannelAttrKey;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMLoginInfoModel;
import com.developer.im.model.IMSendMessageInfoModel;
import com.developer.im.netty.service.IMStartServer;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LoginProcessor extends AbstractMessageProcessor<IMLoginInfoModel>{

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, IMLoginInfoModel loginInfo) {

        OAuth2AccessToken oAuth2AccessToken = null;
        IMSendMessageInfoModel sendMessageInfo = new IMSendMessageInfoModel();
        sendMessageInfo.setCmd(IMCmdType.FORCE_LOGOUT.code());
        try {
            oAuth2AccessToken = ResourceServerConfiger.resourceServerSecurityConfigurer.getTokenStore().readAccessToken(loginInfo.getAccessToken());
            Date now = new Date();
            if(oAuth2AccessToken.getExpiration().compareTo(now)<0){
                sendMessageInfo.setData("用户token已过期,强制下线重新登录");
                ctx.channel().writeAndFlush(sendMessageInfo);
                ctx.channel().close();
                log.warn("用户token检验失败,强制下线,token:{}",loginInfo.getAccessToken());
                return;
            }
        }catch (Exception e){
            sendMessageInfo.setData("用户token检验失败,强制下线");
            ctx.channel().writeAndFlush(sendMessageInfo);
            ctx.channel().close();
            log.warn("用户token检验失败,强制下线,token:{}",loginInfo.getAccessToken());
            return;
        }

        // 获取用户id以及terminal
        String selfUserInfoContext = oAuth2AccessToken.getAdditionalInformation().get("selfUserInfoKey").toString();
        SelfUserInfoModel selfUserInfoModel = JSON.parseObject(selfUserInfoContext, SelfUserInfoModel.class);
        Long userId = selfUserInfoModel.getUserId();
        Integer terminal = selfUserInfoModel.getTerminal();
        log.info("用户登录,userId:{}",userId);

        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);
        if(context!=null && !ctx.channel().id().equals(context.channel().id())){
            // 不允许多地登录,强制下线
            sendMessageInfo.setData("你在其他地方登录,将被强制下线");
            context.channel().writeAndFlush(sendMessageInfo);
            log.info("异地登录，强制下线,userId:{}",userId);
            return;
        }

        // 绑定用户和channel
        UserChannelCtxMap.addChannelCtx(userId,terminal,ctx);
        // 设置用户id属性
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        ctx.channel().attr(userIdAttr).set(userId);
        // 设置用户终端类型
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        ctx.channel().attr(terminalAttr).set(terminal);
        // 初始化心跳次数
        AttributeKey<Long> hearBbeatAttr = AttributeKey.valueOf(ChannelAttrKey.HEARTBEAT_TIMES);
        ctx.channel().attr(hearBbeatAttr).set(0L);
        // 在redis上记录每个user的channelId，15秒没有心跳，则自动过期
        String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
        redisTemplate.opsForValue().set(key, IMStartServer.serverId, DeveloperConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        // 响应ws
        IMSendMessageInfoModel sendInfo = new IMSendMessageInfoModel();
        sendInfo.setCmd(IMCmdType.LOGIN.code());
        ctx.channel().writeAndFlush(sendInfo);
    }

    @Override
    public IMLoginInfoModel trans(Object object) {
        return BeanUtil.fillBeanWithMap((HashMap) object, new IMLoginInfoModel(), false);
    }
}
