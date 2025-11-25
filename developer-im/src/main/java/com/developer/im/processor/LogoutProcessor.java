package com.developer.im.processor;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.message.MessageTerminalTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.constant.ChannelAttrKey;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogoutProcessor  extends AbstractMessageProcessor<Object>{

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, Object data) {
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        Long userId = ctx.channel().attr(userIdAttr).get();
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        Integer terminal = ctx.channel().attr(terminalAttr).get();
        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);

        if(context!=null && ctx.channel().id().equals(context.channel().id())){
            UserChannelCtxMap.removeChannelCtx(userId,terminal);
            String key = String.join(":", RedisKeyConstant.IM_MAX_SERVER_ID,userId.toString(),terminal.toString());
            redisTemplate.delete(key);
            log.info("用户断开链接,userid:{},终端类型：{}",userId, MessageTerminalTypeEnum.fromCode(terminal).desc());
        }
    }

    @Override
    public DeveloperResult<Boolean> handler(Object data) {
        return null;
    }
}
