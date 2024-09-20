package com.developer.framework.constant;

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
     * 用户私聊最大消息id
     * @param userId
     * @return
     */
    public static String DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(Long userId){
        return String.format("developer:message:private:user:%s:max:id",userId);
    }
}
