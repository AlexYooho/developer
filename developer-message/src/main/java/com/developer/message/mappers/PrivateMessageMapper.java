package com.developer.message.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.message.pojo.PrivateMessagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessagePO> {

    List<PrivateMessagePO> findMessageList(@Param("last_seq") Long lastSeq, @Param("uid_a") Long uidA, @Param("uid_b") Long uidB);

    void modifyMessageStatus(@Param("status") MessageStatusEnum status, @Param("ids") List<Long> ids);

    List<PrivateMessagePO> findMessageByStatus(@Param("uid_a") Long uidA, @Param("uid_b") Long uidB, @Param("message_status") MessageStatusEnum messageStatus);

    PrivateMessagePO findMessageByMessageId(@Param("uid_a")Long uidA, @Param("uid_b") Long uidB, @Param("id")Long messageId);

    PrivateMessagePO findMessageByMessageId2(@Param("id")Long messageId);

    List<PrivateMessagePO> findAllMessageByTarget(@Param("uid_a")Long uidA, @Param("uid_b") Long uidB);

    void updateDeleteStatus(@Param("message_ids")List<Long> messageIds,@Param("delete")Boolean delete);

}
