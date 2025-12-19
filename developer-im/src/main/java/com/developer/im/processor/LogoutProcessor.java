package com.developer.im.processor;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.im.constant.ChannelAttrKey;
import com.developer.im.netty.service.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogoutProcessor extends AbstractMessageProcessor<Object> {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void handler(ChannelHandlerContext ctx, Object data) {
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        Long userId = ctx.channel().attr(userIdAttr).get();
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        Integer terminal = ctx.channel().attr(terminalAttr).get();
        if (userId == null || terminal == null) {
            log.warn("用户登出失败, userId或terminal为空");
            return;
        }
        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);

        if (context != null && ctx.channel().id().equals(context.channel().id())) {
            UserChannelCtxMap.removeChannelCtx(userId, terminal);
            String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
            redisUtil.delete(key);
            // 清理用户和服务端节点的映射关系
            String mapKey = RedisKeyConstant.USER_MAP_SERVER_INFO_KEY(userId);
            redisUtil.hDel(mapKey, terminal.toString());
            log.info("用户断开链接,userid:{},终端类型：{}", userId, TerminalTypeEnum.fromCode(terminal).desc());
        }
    }

    @Override
    public DeveloperResult<Boolean> handler(Object data) {
        return null;
    }
}
