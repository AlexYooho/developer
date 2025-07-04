package com.developer.im.netty.service;

import com.developer.im.constant.ChannelAttrKey;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMSendMessageInfoModel;
import com.developer.im.processor.AbstractMessageProcessor;
import com.developer.im.processor.ProcessorFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class IMChannelHandler extends SimpleChannelInboundHandler<IMSendMessageInfoModel> {

    /**
     * 接收消息
     * @param channelHandlerContext
     * @param imSendMessageInfoModel
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMSendMessageInfoModel imSendMessageInfoModel) throws Exception {
        AbstractMessageProcessor processor = ProcessorFactory.getHandler(imSendMessageInfoModel.getCmd());
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
        AbstractMessageProcessor processor = ProcessorFactory.getHandler(IMCmdType.LOGOUT);
        processor.handler(ctx, Optional.ofNullable(null));
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
