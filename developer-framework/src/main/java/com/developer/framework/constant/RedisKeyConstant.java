package com.developer.framework.constant;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.VerifyCodeTypeEnum;

public class RedisKeyConstant {

    /**
     * im-server最大id,从0开始递增
     */
    public final static String  IM_MAX_SERVER_ID = "im:max_server_id";

    /**
     * 用户ID所连接的IM-server的ID
     */
    public final static String  IM_USER_SERVER_ID = "im:user:server_id";

    /**
     * 群聊消息发送结果队列
     */
    public final static String IM_RESULT_GROUP_QUEUE = "im:result:group";

    /**
     * 私聊消息发送结果队列
     */
    public final static String IM_RESULT_PRIVATE_QUEUE = "im:result:private";

    /**
     * 未读私聊消息队列
     */
    public final static String IM_MESSAGE_PRIVATE_QUEUE = "im:message:private";

    /**
     * 已读群聊消息位置(已读最大id)
     */
    public final static String IM_GROUP_READED_POSITION = "im:readed:group:position";

    /**
     * 用户私聊最大消息id（多端）
     */
    public static String DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(Long userId, com.developer.framework.enums.MessageTerminalTypeEnum terminalType) {
        return String.format("developer:message:private:user:%s:max:id:%s", userId, terminalType == null ? "UNKNOWN" : terminalType.name());
    }

    /**
     * 点赞记录key
     */
    public static String MESSAGE_LIKE_KEY(MessageMainTypeEnum messageMainTypeEnum, Long messageId, Long userId){
        return String.format("developer:message:like:%s:%s:%s",messageMainTypeEnum.code(),messageId,userId);
    }

    /**
     * 用户点赞记录key
     */
    public static String MESSAGE_LIKE_USER_KEY(MessageMainTypeEnum messageMainTypeEnum, Long messageId, Long userId){
        return String.format("developer:message:like:%s:%s:user:%s",messageMainTypeEnum.code(),messageId,userId);
    }

    /**
     * 消息点赞key
     */
    public static String MESSAGE_LIKE_MESSAGE_KEY(MessageMainTypeEnum messageMainTypeEnum, Long messageId){
        return String.format("developer:message:like:%s:%s",messageMainTypeEnum.code(),messageId);
    }

    /**
     * 抢红包锁
     */
    public static String OPEN_RED_PACKETS_LOCK_KEY(Long redPacketsId){
        return String.format("developer:payment:red:packets:open:lock:%s",redPacketsId);
    }

    /**
     * 红包信息key
     */
    public static String RED_PACKETS_INFO_KEY(Long redPacketsId){
    	return String.format("developer:payment:red:packets:info:%s",redPacketsId);
    }

    /**
     * 用户注册验证码
     */
    public static String verifyCode(VerifyCodeTypeEnum verifyCodeType, String account){
    	return String.format("developer:user:verify:type:%s:%s",verifyCodeType.code(),account);
    }

    /**
     * 存在好友关系
     */
    public static String IS_FRIEND_KEY(Long userId,Long friendId){
        return String.format("developer:message:user:%s:friend:%s:exist:relation",userId,friendId);
    }

    /**
     * 维护 用户:服务端节点信息
     * 数据类型：hash
     * 用户ID-> ChannelID:服务端节点信息
     * user_id-> ios_id:127.0.0.1:8080
     * user_id-> pc_id:127.0.0.1:8081
     * user_id-> android_id:127.0.0.1:8082
     */
    public static String USER_MAP_SERVER_INFO_KEY(Long userId){
        return "developer:im:user:map:server:info:".concat(userId.toString());
    }

    // 好友列表key
    public static String FRIENDS_KEY(Long userId){
        return "developer:friend:list:user:".concat(userId.toString());
    }
}
