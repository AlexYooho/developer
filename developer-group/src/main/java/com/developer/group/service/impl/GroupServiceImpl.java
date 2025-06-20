package com.developer.group.service.impl;

import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.IMOnlineUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.group.client.FriendClient;
import com.developer.group.dto.*;
import com.developer.group.pojo.GroupInfoPO;
import com.developer.group.pojo.GroupMemberPO;
import com.developer.group.repository.GroupInfoRepository;
import com.developer.group.repository.GroupMemberRepository;
import com.developer.group.service.GroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private FriendClient friendClient;

    @Autowired
    private IMOnlineUtil imOnlineUtil;

    @Override
    public DeveloperResult<CreateGroupRequestDTO> createGroup(CreateGroupRequestDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        GroupInfoPO group = BeanUtils.copyProperties(dto, GroupInfoPO.class);
        assert group != null;
        group.setOwnerId(userId);
        groupInfoRepository.save(group);

        GroupMemberPO groupMember = new GroupMemberPO();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(userId);
        groupMember.setAliasName(StringUtils.isEmpty(dto.getAliasName()) ? nickName : dto.getAliasName());
        groupMember.setRemark(StringUtils.isEmpty(dto.getRemark())?group.getName():dto.getRemark());
        this.groupMemberRepository.save(groupMember);

        dto.setId(group.getId());
        dto.setAliasName(groupMember.getAliasName());
        dto.setRemark(groupMember.getRemark());

        return DeveloperResult.success(serialNo,dto);
    }

    @Override
    public DeveloperResult<CreateGroupRequestDTO> modifyGroup(CreateGroupRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        GroupInfoPO group = groupInfoRepository.getById(req.getId());
        if(group==null){
            return DeveloperResult.error(serialNo,"群不存在,请确认再操作");
        }

        if(group.getOwnerId().equals(userId)){
            group = BeanUtils.copyProperties(req,GroupInfoPO.class);
            groupInfoRepository.updateById(group);
        }

        GroupMemberPO member = groupMemberRepository.findByGroupIdAndUserId(req.getId(), userId);
        if(member==null){
            return DeveloperResult.error(serialNo,"你不是群聊的成员");
        }

        member.setAliasName(StringUtils.isEmpty(req.getAliasName())? nickName : req.getAliasName());
        member.setRemark(StringUtils.isEmpty(req.getRemark())?group.getName():req.getRemark());
        groupMemberRepository.updateById(member);

        return DeveloperResult.success(serialNo,req);
    }

    @Override
    public DeveloperResult<Boolean> deleteGroup(DissolveGroupRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        GroupInfoPO group = this.groupInfoRepository.getById(req.getGroupId());
        if(!group.getOwnerId().equals(userId)){
            return DeveloperResult.error(serialNo,"您不是群主,只有群主才能解散");
        }

        group.setDeleted(true);
        this.groupInfoRepository.updateById(group);

        this.groupMemberRepository.removeByGroupId(req.getGroupId());

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<GroupInfoDTO> findById(FindGroupRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        GroupInfoPO group = groupInfoRepository.getById(req.getGroupId());
        GroupMemberPO groupMember = groupMemberRepository.findByGroupIdAndUserId(req.getGroupId(), userId);
        if(groupMember==null){
            return DeveloperResult.error(serialNo,"您未加入群聊");
        }

        GroupInfoDTO dto = BeanUtils.copyProperties(group, GroupInfoDTO.class);
        assert dto != null;
        dto.setAliasName(groupMember.getAliasName());
        dto.setRemark(groupMember.getRemark());
        return DeveloperResult.success(serialNo,dto);
    }

    @Override
    public DeveloperResult<List<GroupInfoDTO>> findGroupList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        List<GroupMemberPO> groupMembers = groupMemberRepository.findByUserId(userId);
        if(groupMembers.isEmpty()){
            return DeveloperResult.success(serialNo);
        }

        List<Long> ids = groupMembers.stream().map((GroupMemberPO::getGroupId)).collect(Collectors.toList());
        List<GroupInfoPO> groups = groupInfoRepository.findByGroupIds(ids);
        List<GroupInfoDTO> list = groups.stream().map(x -> {
            GroupInfoDTO groupInfoRep = BeanUtils.copyProperties(x, GroupInfoDTO.class);
            GroupMemberPO member = groupMembers.stream().filter(m -> x.getId().equals(m.getGroupId())).findFirst().get();
            assert groupInfoRep != null;
            groupInfoRep.setAliasName(member.getAliasName());
            groupInfoRep.setRemark(member.getRemark());
            return groupInfoRep;
        }).collect(Collectors.toList());
        return DeveloperResult.success(serialNo,list);
    }

    @Override
    public DeveloperResult<Boolean> invite(GroupInviteRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        GroupInfoPO group = this.groupInfoRepository.getById(req.getGroupId());
        if(group==null){
            return DeveloperResult.error(serialNo,"群聊不存在");
        }

        List<GroupMemberPO> members = this.groupMemberRepository.findByGroupId(req.getGroupId());
        long size = members.stream().filter(x -> !x.getQuit()).count();
        if(req.getFriendIds().size()+size> DeveloperConstant.MAX_GROUP_MEMBER){
            return DeveloperResult.error(serialNo,"群聊人数不能大于"+DeveloperConstant.MAX_GROUP_MEMBER+"人");
        }

        DeveloperResult<List<FriendInfoDTO>> developerResult = friendClient.friends(serialNo);
        List<FriendInfoDTO> friends = (List<FriendInfoDTO>) developerResult.getData();
        List<FriendInfoDTO> friendsList = req.getFriendIds().stream().map(id -> friends.stream().filter(f -> f.getId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if(friendsList.size()!=req.getFriendIds().size()){
            return DeveloperResult.error(serialNo,"部分用户不是您的好友,邀请失败");
        }

        List<GroupMemberPO> groupMembers = friendsList.stream().map(f -> {
            Optional<GroupMemberPO> optional = members.stream().filter(m -> m.getUserId().equals(f.getId())).findFirst();
            GroupMemberPO groupMember = optional.orElseGet(GroupMemberPO::new);
            groupMember.setGroupId(req.getGroupId());
            groupMember.setUserId(f.getId());
            groupMember.setAliasName(f.getNickName());
            groupMember.setRemark(group.getName());
            groupMember.setHeadImage(f.getHeadImage());
            groupMember.setCreatedTime(new Date());
            groupMember.setQuit(false);
            return groupMember;
        }).collect(Collectors.toList());

        if(!groupMembers.isEmpty()){
            groupMemberRepository.saveOrUpdateBatch(groupMembers);
        }

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<List<GroupMemberDTO>> findGroupMembers(FindGroupMembersRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        List<GroupMemberPO> members = this.groupMemberRepository.findByGroupId(req.getGroupId());
        List<Long> userIds = members.stream().map(GroupMemberPO::getUserId).collect(Collectors.toList());
        List<Long> onlineUserIds = imOnlineUtil.getOnlineUser(userIds);
        List<GroupMemberDTO> list = members.stream().map(x -> {
            GroupMemberDTO vo = BeanUtils.copyProperties(x, GroupMemberDTO.class);
            assert vo != null;
            vo.setOnline(onlineUserIds.contains(x.getUserId()));
            return vo;
        }).collect(Collectors.toList());
        return DeveloperResult.success(serialNo,list);
    }

    @Override
    public DeveloperResult<Boolean> quitGroup(QuitGroupRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        GroupInfoPO group = groupInfoRepository.getById(req.getGroupId());
        if(group.getOwnerId().equals(userId)){
            return DeveloperResult.error(serialNo,"你是群主,不能退出");
        }

        this.groupMemberRepository.removeByGroupAndUserId(req.getGroupId(),userId);
        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<Boolean> kickGroup(KickOutGroupRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        GroupInfoPO group = groupInfoRepository.getById(req.getGroupId());
        if(!group.getOwnerId().equals(userId)){
            return DeveloperResult.error(serialNo,"你不是群主，不能踢人");
        }

        if(req.getUserId().equals(userId)){
            return DeveloperResult.error(serialNo,"你不能踢自己");
        }

        groupMemberRepository.removeByGroupAndUserId(req.getGroupId(),req.getUserId());
        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<List<SelfJoinGroupInfoDTO>> findSelfJoinAllGroupInfo() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        List<SelfJoinGroupInfoDTO> joinAllGroupInfoList = groupInfoRepository.findUserJoinGroupInfo(userId);
        return DeveloperResult.success(serialNo,joinAllGroupInfoList);
    }
}
