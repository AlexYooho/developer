package com.developer.im.netty.service;

import com.developer.framework.utils.RedisUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserChannelCtxMap {

    /**
     * 维护userId和ctx的关联关系，格式:Map<userId,map<terminal，ctx>>
     */
    private static Map<Long, Map<Integer, ChannelHandlerContext>> channelMap=new ConcurrentHashMap<>();

    public static void addChannelCtx(Long userId,Integer channel,ChannelHandlerContext ctx){
        channelMap.computeIfAbsent(userId,x->new ConcurrentHashMap<>()).put(channel,ctx);
    }

    public static void removeChannelCtx(Long userId,Integer terminal){
        if(userId!=null && terminal!=null && channelMap.containsKey(userId)){
            Map<Integer, ChannelHandlerContext> userChannelMap = channelMap.get(userId);
            userChannelMap.remove(terminal);
        }
    }

    public static ChannelHandlerContext getChannelCtx(Long userId,Integer terminal){
        if(userId!=null && terminal!=null && channelMap.containsKey(userId)){
            Map<Integer, ChannelHandlerContext> userChannelMap = channelMap.get(userId);
            if(userChannelMap.containsKey(terminal)){
                return userChannelMap.get(terminal);
            }
        }
        return null;
    }


}