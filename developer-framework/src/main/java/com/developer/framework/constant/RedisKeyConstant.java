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
     * @param userId
     * @param terminalType
     * @return
     */
    public static String DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(Long userId, com.developer.framework.enums.MessageTerminalTypeEnum terminalType) {
        return String.format("developer:message:private:user:%s:max:id:%s", userId, terminalType == null ? "UNKNOWN" : terminalType.name());
    }

    /**
     * 点赞记录key
     * @param messageMainTypeEnum
     * @param messageId
     * @param userId
     * @return
     */
    public static String MESSAGE_LIKE_KEY(MessageMainTypeEnum messageMainTypeEnum, Long messageId, Long userId){
        return String.format("developer:message:like:%s:%s:%s",messageMainTypeEnum.code(),messageId,userId);
    }

    /**
     * 用户点赞记录key
     * @param messageMainTypeEnum
     * @param messageId
     * @param userId
     * @return
     */
    public static String MESSAGE_LIKE_USER_KEY(MessageMainTypeEnum messageMainTypeEnum, Long messageId, Long userId){
        return String.format("developer:message:like:%s:%s:user:%s",messageMainTypeEnum.code(),messageId,userId);
    }

    /**
     * 消息点赞key
     * @param messageMainTypeEnum
     * @param messageId
     * @return
     */
    public static String MESSAGE_LIKE_MESSAGE_KEY(MessageMainTypeEnum messageMainTypeEnum, Long messageId){
        return String.format("developer:message:like:%s:%s",messageMainTypeEnum.code(),messageId);
    }

    /**
     * 抢红包锁
     * @param redPacketsId
     * @return
     */
    public static String OPEN_RED_PACKETS_LOCK_KEY(Long redPacketsId){
        return String.format("developer:payment:red:packets:open:lock:%s",redPacketsId);
    }

    /**
     * 红包信息key
     * @param redPacketsId
     * @return
     */
    public static String RED_PACKETS_INFO_KEY(Long redPacketsId){
    	return String.format("developer:payment:red:packets:info:%s",redPacketsId);
    }

    /**
     * 用户注册验证码
     * @param account
     * @return
     */
    public static String verifyCode(VerifyCodeTypeEnum verifyCodeType, String account){
    	return String.format("developer:user:verify:type:%s:%s",verifyCodeType.code(),account);
    }
}
