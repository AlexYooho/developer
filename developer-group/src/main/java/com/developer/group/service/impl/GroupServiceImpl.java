package com.developer.group.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.group.GroupInviteTypeEnum;
import com.developer.framework.enums.group.GroupMemberRoleEnum;
import com.developer.framework.enums.group.GroupTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.IMOnlineUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.group.dto.*;
import com.developer.group.dto.group.CreateGroupRequestDTO;
import com.developer.group.dto.group.CreateGroupResponseDTO;
import com.developer.group.dto.group.ModifyGroupInfoRequestDTO;
import com.developer.group.pojo.GroupInfoPO;
import com.developer.group.pojo.GroupMemberPO;
import com.developer.group.repository.GroupInfoRepository;
import com.developer.group.repository.GroupMemberRepository;
import com.developer.group.service.GroupMemberService;
import com.developer.group.service.GroupService;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.friend.response.FriendInfoResponseRpcDTO;
import com.developer.rpc.dto.message.request.SendJoinGroupInviteMessageRequestRpcDTO;
import com.developer.rpc.dto.user.request.UserInfoRequestRpcDTO;
import com.developer.rpc.dto.user.response.UserInfoResponseRpcDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final GroupMemberService groupMemberService;
    private final IMOnlineUtil imOnlineUtil;
    private final RpcClient rpcClient;

    /*
    建群
     */
    @Override
    public DeveloperResult<CreateGroupResponseDTO> createGroup(CreateGroupRequestDTO dto) {
        // 建群
        GroupInfoPO group = new GroupInfoPO();
        dto.getMemberUserIds().add(SelfUserInfoContext.selfUserInfo().getUserId());
        group.setGroupName(dto.getGroupName());
        group.setOwnerId(SelfUserInfoContext.selfUserInfo().getUserId());
        group.setGroupType(GroupTypeEnum.NORMAL);
        group.setInviteType(GroupInviteTypeEnum.PASS);
        group.setMaxMemberCount(500);
        group.setMemberCount(dto.getMemberUserIds().size());
        group.setMuteAll(false);
        group.setDeleted(false);
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        group.setRemark("");
        groupInfoRepository.save(group);

        // 处理群成员
        DeveloperResult<Boolean> initMemberInfo = groupMemberService.createGroupInitMemberInfo(group.getId(), group.getOwnerId(), dto.getMemberUserIds());
        if (!initMemberInfo.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), initMemberInfo.getMsg());
        }

        // 返回群信息
        CreateGroupResponseDTO responseDTO = new CreateGroupResponseDTO();
        responseDTO.setGroupId(group.getId());
        responseDTO.setGroupName(group.getGroupName());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), responseDTO);
    }

    /*
    修改群信息
     */
    @Override
    public DeveloperResult<Boolean> modifyGroup(ModifyGroupInfoRequestDTO req) {
        // 判断群是否存在
        GroupInfoPO group = groupInfoRepository.getById(req.getGroupId());
        if (group == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "群不存在,请确认再操作");
        }

        // 校验群主管理员信息
        DeveloperResult<Boolean> verifyResult = verifyChatroomOwnerInfo(group.getId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if (!verifyResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), verifyResult.getMsg());
        }

        // 修改群信息
        group.setGroupName(req.getGroupName() == null ? group.getGroupName() : req.getGroupName());
        group.setGroupAvatar(req.getGroupAvatar() == null ? group.getGroupAvatar() : req.getGroupAvatar());
        group.setOwnerId(req.getOwnerId() == null ? group.getOwnerId() : req.getOwnerId());
        group.setInviteType(req.getInviteType() == null ? group.getInviteType() : req.getInviteType());
        group.setNotice(req.getNotice() == null ? group.getNotice() : req.getNotice());
        group.setMuteAll(req.getMuteAll() == null ? group.getMuteAll() : req.getMuteAll());
        groupInfoRepository.updateById(group);

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    解散群
     */
    @Override
    public DeveloperResult<Boolean> deleteGroup(DissolveGroupRequestDTO req) {
        // 查询群是否存在
        GroupInfoPO group = this.groupInfoRepository.getById(req.getGroupId());
        if(ObjectUtil.isEmpty(group)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"群不存在");
        }

        // 检测是否为群主
        if (!group.getOwnerId().equals(SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "仅群主可操作");
        }

        // 修改群状态为解散
        group.setDeleted(true);
        this.groupInfoRepository.updateById(group);

        // 清空相关群缓存


        // 回滚红包交易等未完成业务


        // 发送最后群消息通知成员群已解散


        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    根据id查群信息
     */
    @Override
    public DeveloperResult<GroupInfoDTO> findById(FindGroupRequestDTO req) {
        // 群是否存在
        GroupInfoPO group = groupInfoRepository.getById(req.getGroupId());
        if(ObjectUtil.isEmpty(group)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"群不存在");
        }

        // 是否为群成员
        DeveloperResult<GroupMemberPO> groupMemberInfo = groupMemberService.findGroupMemberInfo(req.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if(!groupMemberInfo.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),groupMemberInfo.getMsg());
        }
        GroupMemberPO groupMember = groupMemberInfo.getData();

        // 查询内容
        GroupInfoDTO dto = new GroupInfoDTO();
        dto.setGroupName(group.getGroupName());
        dto.setGroupOwnerId(group.getOwnerId());
        dto.setGroupAvatar(group.getGroupAvatar());
        dto.setNotice(group.getNotice());
        dto.setGroupMemberAlias(groupMember.getAlias());
        dto.setRemark(groupMember.getRemark());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), dto);
    }

    /*
    获取当前用户群列表
     */
    @Override
    public DeveloperResult<List<GroupInfoDTO>> findGroupList() {
        // 获取当前登录用户加入的所有群
        List<GroupMemberPO> groupMembers = groupMemberService.findJoinGroupList(SelfUserInfoContext.selfUserInfo().getUserId()).getData();
        if (groupMembers.isEmpty()) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo());
        }

        // 群id集合
        List<Long> ids = groupMembers.stream().map((GroupMemberPO::getGroupId)).collect(Collectors.toList());

        // 获取群信息
        List<GroupInfoPO> groups = groupInfoRepository.findByGroupInfo(ids);

        // 聚合群、成员信息
        List<GroupInfoDTO> list = groups.stream().map(x -> {
            GroupInfoDTO groupInfo = new GroupInfoDTO();
            groupInfo.setGroupName(x.getGroupName());
            groupInfo.setGroupAvatar(x.getGroupAvatar());
            groupInfo.setGroupOwnerId(x.getOwnerId());
            groupInfo.setGroupType(x.getGroupType());
            groupInfo.setMaxMemberCount(x.getMaxMemberCount());
            groupInfo.setMemberCount(x.getMemberCount());
            groupInfo.setNotice(x.getNotice());
            groupInfo.setMuteAll(x.getMuteAll());
            groupInfo.setRemark(x.getRemark());

            GroupMemberPO memberPO = groupMembers.stream().filter(xx -> xx.getGroupId().equals(x.getId())).findFirst().orElse(new GroupMemberPO());
            groupInfo.setMemberRole(memberPO.getMemberRole());
            groupInfo.setGroupMemberAlias(memberPO.getAlias());
            groupInfo.setJoinTime(memberPO.getJoinTime());
            groupInfo.setJoinType(memberPO.getJoinType());
            groupInfo.setIsMuted(memberPO.getIsMuted());
            return groupInfo;
        }).collect(Collectors.toList());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }

    /*
    邀请入群
     */
    @Override
    public DeveloperResult<Boolean> invite(GroupInviteRequestDTO req) {
        // 群是否存在
        GroupInfoPO group = this.groupInfoRepository.getById(req.getGroupId());
        if (group == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "群聊不存在");
        }

        // 校验是否超过群聊限制人数
        if (req.getFriendIds().size() + group.getMemberCount() > DeveloperConstant.MAX_GROUP_MEMBER) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "群聊人数不能大于" + DeveloperConstant.MAX_GROUP_MEMBER + "人");
        }

        // rpc调用获取好友信息
        DeveloperResult<List<FriendInfoResponseRpcDTO>> rpcExecuteResult = RpcExecutor.execute(() -> rpcClient.friendRpcService.findFriends());
        if(!rpcExecuteResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),rpcExecuteResult.getMsg());
        }

        List<FriendInfoResponseRpcDTO> friendInfoDTO = rpcExecuteResult.getData();

        // 校验邀请用户是否全部都是自己好友
        List<FriendInfoResponseRpcDTO> friendsList = req.getFriendIds().stream().map(id -> friendInfoDTO.stream().filter(f -> f.getId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if (friendsList.size() != req.getFriendIds().size()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "部分用户不是您的好友,邀请失败");
        }

        // 发送入群邀请消息,待用户确认加入
        SendJoinGroupInviteMessageRequestRpcDTO inviteMessageDTO = new SendJoinGroupInviteMessageRequestRpcDTO();
        inviteMessageDTO.setGroupId(group.getId());
        inviteMessageDTO.setGroupName(group.getGroupName());
        inviteMessageDTO.setGroupAvatar(group.getGroupAvatar());
        inviteMessageDTO.setInviterId(SelfUserInfoContext.selfUserInfo().getUserId());
        inviteMessageDTO.setInviterName(SelfUserInfoContext.selfUserInfo().getNickName());
        inviteMessageDTO.setInviteMemberIds(friendsList.stream().map(FriendInfoResponseRpcDTO::getId).collect(Collectors.toList()));
        DeveloperResult<Boolean> inviteResult = RpcExecutor.execute(() -> rpcClient.messageRpcService.sendJoinGroupInviteMessage(inviteMessageDTO));
        if(!inviteResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),inviteResult.getMsg());
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    获取群成员信息列表
     */
    @Override
    public DeveloperResult<List<GroupMemberDTO>> findGroupMembers(FindGroupMembersRequestDTO req) {
        // 获取当前群的所有群成员
        List<GroupMemberPO> groupMembers = groupMemberService.findGroupMember(req.getGroupId()).getData();

        // 群成员用户id
        List<Long> memberUserIds = groupMembers.stream().map(GroupMemberPO::getUserId).collect(Collectors.toList());

        // im 在线状态
        List<Long> onlineUserIds = imOnlineUtil.getOnlineUser(memberUserIds);

        // rpc 调用用户信息
        UserInfoRequestRpcDTO userRpcDTO = new UserInfoRequestRpcDTO();
        userRpcDTO.setUserIds(memberUserIds);
        DeveloperResult<List<UserInfoResponseRpcDTO>> execute = RpcExecutor.execute(() -> rpcClient.userRpcService.findUserInfo(userRpcDTO));
        if(!execute.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),execute.getMsg());
        }

        // 消息聚合
        List<GroupMemberDTO> list = groupMembers.stream().map(x -> {
            GroupMemberDTO vo = new GroupMemberDTO();
            vo.setUserId(x.getUserId());
            vo.setAliasName(x.getAlias());
            vo.setQuit(x.getQuit());
            vo.setOnline(onlineUserIds.contains(x.getUserId()));

            UserInfoResponseRpcDTO memberUserInfo = execute.getData().stream().filter(xx -> xx.getUserId().equals(x.getUserId())).findFirst().orElse(new UserInfoResponseRpcDTO());
            vo.setHeadImage(memberUserInfo.getAvatar());
            return vo;
        }).collect(Collectors.toList());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }

    /*
    退出群聊
     */
    @Override
    public DeveloperResult<Boolean> quitGroup(QuitGroupRequestDTO req) {
        // 校验群主
        GroupInfoPO group = groupInfoRepository.getById(req.getGroupId());
        if (group.getOwnerId().equals(SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "你是群主,不能退出");
        }

        // 修改群成员状态
        ModifyGroupMemberInfoDTO memberInfoDTO = new ModifyGroupMemberInfoDTO();
        memberInfoDTO.setGroupId(group.getId());
        memberInfoDTO.setMemberUserId(SelfUserInfoContext.selfUserInfo().getUserId());
        memberInfoDTO.setQuit(true);
        DeveloperResult<Boolean> modifyResult = groupMemberService.modifyGroupMemberInfo(memberInfoDTO);
        if(!modifyResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),modifyResult.getMsg());
        }

        // 修改群成员数量
        group.setMemberCount(group.getMemberCount()-1);
        groupInfoRepository.updateById(group);

        // 通知所有群成员,发送IM消息
        DeveloperResult<Boolean> quitGroupResult = RpcExecutor.execute(() -> rpcClient.messageRpcService.sendQuitGroupChatMessage(group.getId()));
        if(!quitGroupResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),quitGroupResult.getMsg());
        }

        this.groupMemberRepository.removeByGroupAndUserId(req.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId());
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    踢人出群
     */
    @Override
    public DeveloperResult<Boolean> kickGroup(KickOutGroupRequestDTO req) {
        // 判断踢出用户是否在当前群聊当中
        DeveloperResult<GroupMemberPO> targetGroupMemberInfoResult = groupMemberService.findGroupMemberInfo(req.getGroupId(), req.getUserId());
        if(!targetGroupMemberInfoResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),targetGroupMemberInfoResult.getMsg());
        }

        // 获取当前用户所在群的成员信息
        DeveloperResult<GroupMemberPO> currentUserGroupMemberInfoResult = groupMemberService.findGroupMemberInfo(req.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if(!currentUserGroupMemberInfoResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),currentUserGroupMemberInfoResult.getMsg());
        }

        // 校验当前用户的群角色身份,是否为群主或管理员
        GroupMemberPO memberInfo = currentUserGroupMemberInfoResult.getData();
        if(!memberInfo.getMemberRole().equals(GroupMemberRoleEnum.OWNER) && !memberInfo.getMemberRole().equals(GroupMemberRoleEnum.ADMIN)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"仅群主或管理员可操作");
        }

        // 踢出对象不能是当前用户
        if (req.getUserId().equals(SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不能踢出自己");
        }

        // 修改群成员状态信息
        ModifyGroupMemberInfoDTO modifyDTO = new ModifyGroupMemberInfoDTO();
        modifyDTO.setGroupId(req.getGroupId());
        modifyDTO.setMemberUserId(req.getUserId());
        modifyDTO.setQuit(false);
        DeveloperResult<Boolean> modifyResult = groupMemberService.modifyGroupMemberInfo(modifyDTO);
        if(!modifyResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),modifyResult.getMsg());
        }

        // 更新群成员数量
        GroupInfoPO groupInfo = groupInfoRepository.getById(req.getGroupId());
        groupInfo.setMemberCount(groupInfo.getMemberCount()-1);
        groupInfoRepository.updateById(groupInfo);

        // 关播通知 xx 被踢出群聊
        DeveloperResult<Boolean> execute = RpcExecutor.execute(() -> rpcClient.messageRpcService.sendKickGroupMessage(groupInfo.getId()));
        if(!execute.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),execute.getMsg());
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    获取当前用户加入的所有群聊
     */
    @Override
    public DeveloperResult<List<SelfJoinGroupInfoDTO>> findSelfJoinAllGroupInfo() {
        // 查询当前用户未退出的所有群
        DeveloperResult<List<GroupMemberPO>> joinGroupList = groupMemberService.findJoinGroupList(SelfUserInfoContext.selfUserInfo().getUserId());
        if(!joinGroupList.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),joinGroupList.getMsg());
        }

        // 群id集合
        List<Long> groupIds = joinGroupList.getData().stream().map(GroupMemberPO::getGroupId).collect(Collectors.toList());

        // 查询群信息
        List<GroupInfoPO> groupInfoList = CollUtil.isNotEmpty(groupIds) ? groupInfoRepository.findByGroupInfo(groupIds) : new ArrayList<>();

        // 群信息聚合
        List<SelfJoinGroupInfoDTO> aggregatorData = joinGroupList.getData().stream().map(x -> {
            SelfJoinGroupInfoDTO dto = new SelfJoinGroupInfoDTO();
            // 用户群成员信息
            dto.setUserId(x.getUserId());
            dto.setQuit(x.getQuit());
            dto.setMemberAlias(x.getAlias());
            dto.setGroupRole(x.getMemberRole());

            // 群信息
            GroupInfoPO groupInfo = groupInfoList.stream().filter(xx -> xx.getId().equals(x.getGroupId())).findFirst().orElse(new GroupInfoPO());
            dto.setGroupId(groupInfo.getId());
            dto.setGroupName(groupInfo.getGroupName());
            dto.setGroupAvatar(groupInfo.getGroupAvatar());
            dto.setCreateTime(groupInfo.getCreateTime());

            return dto;
        }).collect(Collectors.toList());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), aggregatorData);
    }

    /*
    校验群主管理员信息
     */
    @Override
    public DeveloperResult<Boolean> verifyChatroomOwnerInfo(Long groupId, Long userId) {
        DeveloperResult<GroupMemberPO> groupMemberInfo = groupMemberService.findGroupMemberInfo(groupId, userId);
        if (!groupMemberInfo.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupMemberInfo.getMsg());
        }
        GroupMemberPO memberData = groupMemberInfo.getData();
        if (!memberData.getMemberRole().equals(GroupMemberRoleEnum.OWNER) && !memberData.getMemberRole().equals(GroupMemberRoleEnum.ADMIN)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "仅群主管理员可操作");
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
