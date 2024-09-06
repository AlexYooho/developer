package com.developer.friend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.FriendInfoDTO;
import com.developer.friend.dto.NewFriendListDTO;
import com.developer.friend.dto.ProcessAddFriendRequestDTO;
import com.developer.friend.dto.SendAddFriendInfoRequestDTO;
import com.developer.friend.enums.*;
import com.developer.friend.pojo.FriendApplicationRecordPO;
import com.developer.friend.pojo.FriendPO;
import com.developer.friend.repository.FriendApplicationRecordPORepository;
import com.developer.friend.repository.FriendRepository;
import com.developer.friend.service.FriendService;
import com.developer.friend.util.RabbitMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendApplicationRecordPORepository friendApplicationRecordPORepository;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Override
    public DeveloperResult findFriendByUserId(Long userId) {
        List<FriendPO> friendList = friendRepository.findFriendByUserId(userId);
        return DeveloperResult.success(friendList);
    }

    @Override
    public DeveloperResult findFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        List<FriendPO> friendList = friendRepository.findFriendByUserId(userId);
        List<FriendInfoDTO> list = friendList.stream().map(x -> {
            FriendInfoDTO rep = new FriendInfoDTO();
            rep.setId(x.getFriendId());
            rep.setNickName(x.getFriendNickName());
            rep.setHeadImage(x.getFriendHeadImage());
            return rep;
        }).collect(Collectors.toList());
        return DeveloperResult.success(list);
    }

    @Override
    public DeveloperResult isFriend(Long userId1, Long userId2) {
        boolean result = friendRepository.isFriend(userId1,userId2);
        return DeveloperResult.success(result);
    }

    @Override
    public DeveloperResult findFriend(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        FriendPO friend = friendRepository.findByFriendId(friendId, userId);
        if (friend == null) {
            return DeveloperResult.error("对方不是你的好友");
        }

        FriendInfoDTO friendInfoRep = new FriendInfoDTO();
        friendInfoRep.setId(friend.getId());
        friendInfoRep.setHeadImage(friend.getFriendHeadImage());
        friendInfoRep.setNickName(friend.getFriendNickName());
        return DeveloperResult.success(friendInfoRep);
    }

    @Override
    public DeveloperResult sendAddFriendRequest(SendAddFriendInfoRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        FriendPO friend = friendRepository.findByFriendId(req.getFriendId(), userId);
        if (!ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error("对方已是你好友");
        }

        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error("不允许添加自己为好友");
        }

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        req.setRemark("你好,我是"+nickName+",加个好友呗");
        FriendApplicationRecordPO record = friendApplicationRecordPORepository.findRecord(req.getFriendId(), userId);
        if (record == null) {
            record = new FriendApplicationRecordPO(userId, req.getFriendId(), req.getAddChannel().code(), AddFriendStatusEnum.SENT.code(), new Date(), new Date(), req.getRemark());
            friendApplicationRecordPORepository.save(record);
        } else if (record.getStatus().equals(AddFriendStatusEnum.SENT.code()) || record.getStatus().equals(AddFriendStatusEnum.VIEWED.code()) || record.getStatus().equals(AddFriendStatusEnum.REJECTED.code())) {
            friendApplicationRecordPORepository.updateStatus(req.getFriendId(), userId, AddFriendStatusEnum.SENT.code());
        } else if (record.getStatus().equals(AddFriendStatusEnum.AGREED.code())) {
            return DeveloperResult.error("已添加该好友,不许重复添加");
        }

        // 发送添加请求
        rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, req.getRemark(), Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND.code(), IMTerminalTypeEnum.WEB, new Date());
        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult processFriendRequest(ProcessAddFriendRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error("不允许添加自己为好友");
        }

        String message = "";
        if (req.getIsAgree()) {
            // 同意,直接绑定好友关系,发送通知成为好友
            bindFriend(userId, req.getFriendId());
            // 发送添加请求
            message = "我们已经是好友啦";
            // 新增消息记录
            // TODO
            //PrivateMessage privateMessage = new PrivateMessage();
            //privateMessage.setMessageStatus(0);
            //privateMessage.setMessageContent("我们已经是好友啦");
            //privateMessage.setSendId(userId);
            //privateMessage.setReceiverId(req.getFriendId());
            //privateMessage.setMessageContentType(0);
            //privateMessage.setSendTime(new Date());
            //privateMessageRepository.save(privateMessage);
        } else {
            // 拒绝,如果拒绝理由不为空则回复消息
            message = req.getRefuseReason();
        }

        // 处理请求记录状态
        AddFriendStatusEnum status = req.getIsAgree() ? AddFriendStatusEnum.AGREED : AddFriendStatusEnum.REJECTED;
        friendApplicationRecordPORepository.updateStatus(userId, req.getFriendId(), status.code());

        if (ObjectUtil.isNotEmpty(message)) {
            rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, message, Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND.code(), IMTerminalTypeEnum.WEB, new Date());
        }

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult deleteFriendByFriendId(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        FriendPO friend = friendRepository.findByFriendId(friendId, userId);
        if (ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error("对方不是你的好友");
        }

        friendRepository.removeById(friend.getId());


        // todo 推送消息，更新全段好友列表
        // privateMessageRepository.deleteChatMessage(session.getId(), friendId);

        return DeveloperResult.success("删除成功");
    }

    @Override
    public DeveloperResult findFriendAddRequestCount() {
        List<FriendApplicationRecordPO> list = friendApplicationRecordPORepository.findRecordByStatus(SelfUserInfoContext.selfUserInfo().getUserId(),AddFriendStatusEnum.SENT);
        return DeveloperResult.success(list.size());
    }

    @Override
    public DeveloperResult findNewFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        //List<NewFriendListDTO> lists = friendApplicationRecordPORepository.findNewFriendList(userId);
        List<NewFriendListDTO> list = new ArrayList<>();
        return DeveloperResult.success(list);
    }

    @Override
    public DeveloperResult updateAddFriendRecordStatus() {
        friendApplicationRecordPORepository.updateStatusSentToViewed(SelfUserInfoContext.selfUserInfo().getUserId());
        return DeveloperResult.success();
    }

    /**
     * 绑定好友关系
     *
     * @param userId
     * @param friendId
     */
    public void bindFriend(Long userId, Long friendId) {
    }
}
