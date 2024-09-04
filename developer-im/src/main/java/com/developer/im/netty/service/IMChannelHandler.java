package com.developer.im.netty.service;

import com.developer.framework.utils.SpringContext;
import com.developer.im.config.ResourceServerConfiger;
import com.developer.im.constant.ChannelAttrKey;
import com.developer.im.constant.RedisKeyConstant;
import com.developer.im.converter.AccessTokenConvertor;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMSendMessageInfoModel;
import com.developer.im.processor.AbstractMessageProcessor;
import com.developer.im.processor.LoginProcessor;
import com.developer.im.processor.ProcessorFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IMChannelHandler extends SimpleChannelInboundHandler<IMSendMessageInfoModel> {


    @Autowired
    RedisTemplate<String,Object> redisTemplate;


    /**
     * 接收消息
     * @param channelHandlerContext
     * @param imSendMessageInfoModel
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMSendMessageInfoModel imSendMessageInfoModel) throws Exception {
        AbstractMessageProcessor processor = ProcessorFactory.getHandler(IMCmdType.transCode(imSendMessageInfoModel.getCmd()));
        processor.handler(channelHandlerContext,processor.trans(imSendMessageInfoModel.getData()));
    }

    /**
     * 异常事件处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
    }

    /**
     * 用户上线事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info(ctx.channel().id().asLongText()+"上线");
    }

    /**
     * 用户下线事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        Long userId = ctx.channel().attr(userIdAttr).get();
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        Integer terminal = ctx.channel().attr(terminalAttr).get();
        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);

        if(context!=null && ctx.channel().id().equals(context.channel().id())){
            UserChannelCtxMap.removeChannelCtx(userId,terminal);
            String key = String.join(":", RedisKeyConstant.IM_MAX_SERVER_ID,userId.toString(),terminal.toString());
            redisTemplate.delete(key);
            log.info("断开链接,userid:{},终端类型：{}",userId,terminal);
        }
    }

    /**
     * 事件监听器
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                AttributeKey<Long> attr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
                Long userId = ctx.channel().attr(attr).get();
                AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
                Integer terminal = ctx.channel().attr(terminalAttr).get();
                log.info("心跳超时，即将断开连接,用户id:{},终端类型:{} ",userId,terminal);
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
