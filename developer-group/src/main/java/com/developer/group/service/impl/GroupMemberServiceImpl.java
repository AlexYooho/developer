package com.developer.group.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.enums.group.GroupMemberJoinTypeEnum;
import com.developer.framework.enums.group.GroupMemberRoleEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.group.dto.BatchModifyGroupMemberInfoRequestDTO;
import com.developer.group.dto.FindGroupMemberUserIdRequestDTO;
import com.developer.group.dto.ModifyGroupMemberInfoDTO;
import com.developer.group.pojo.GroupMemberPO;
import com.developer.group.repository.GroupMemberRepository;
import com.developer.group.service.GroupMemberService;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.user.request.UserInfoRequestRpcDTO;
import com.developer.rpc.dto.user.response.UserInfoResponseRpcDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final RpcClient rpcClient;

    /*
    查找群成员id
     */
    @Override
    public DeveloperResult<List<Long>> findGroupMember(FindGroupMemberUserIdRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        List<GroupMemberPO> members = this.groupMemberRepository.findByGroupId(req.getGroupId());
        List<Long> userIds = members.stream().map(GroupMemberPO::getUserId).collect(Collectors.toList());
        return DeveloperResult.success(serialNo,userIds);
    }

    /*
    查找群成员
     */
    @Override
    public DeveloperResult<List<GroupMemberPO>> findGroupMember(Long groupId) {
        List<GroupMemberPO> members = this.groupMemberRepository.findByGroupId(groupId);
        return DeveloperResult.success(SerialNoHolder.getSerialNo(),members);
    }

    /*
    获取群成员信息
     */
    @Override
    public DeveloperResult<GroupMemberPO> findGroupMemberInfo(Long groupId, Long memberUserId) {
        GroupMemberPO memberInfo = groupMemberRepository.findByGroupIdAndUserId(groupId, memberUserId);
        if(ObjectUtil.isEmpty(memberInfo)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"群成员不存在");
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo(),memberInfo);
    }

    @Override
    public DeveloperResult<List<GroupMemberPO>> findGroupByMember(List<Long> groupIds, Long memberUserId) {
        List<GroupMemberPO> memberInfo = groupMemberRepository.findGroupByMember(groupIds, memberUserId);
        if(CollUtil.isEmpty(memberInfo)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"未查询到用户的群信息");
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo(),memberInfo);
    }

    /*
    批量修改群成员信息
     */
    @Override
    public DeveloperResult<Boolean> batchModifyGroupMemberInfo(BatchModifyGroupMemberInfoRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        List<GroupMemberPO> ll = new ArrayList<>();
        this.groupMemberRepository.updateBatchById(ll);

        return DeveloperResult.success(serialNo);
    }

    /*
    建群初始化群成员
     */
    @Override
    public DeveloperResult<Boolean> createGroupInitMemberInfo(Long groupId,Long ownerId,List<Long> memberIds) {

        // 获取群成员用户信息
        UserInfoRequestRpcDTO rpcRequestDto = new UserInfoRequestRpcDTO();
        rpcRequestDto.setUserIds(memberIds);
        DeveloperResult<List<UserInfoResponseRpcDTO>> memberUserInfoRpcResponse = RpcExecutor.execute(() -> rpcClient.userRpcService.findUserInfo(rpcRequestDto));
        if(!memberUserInfoRpcResponse.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), memberUserInfoRpcResponse.getMsg());
        }

        // 成员用户信息
        List<UserInfoResponseRpcDTO> memberUserInfos = memberUserInfoRpcResponse.getData();

        // 聚合群成员表信息
        List<GroupMemberPO> members = memberUserInfos.stream().map(x -> {
            GroupMemberPO memberPO = new GroupMemberPO();
            memberPO.setGroupId(groupId);
            memberPO.setUserId(x.getUserId());
            memberPO.setAlias(x.getNickName()); // 群成员昵称
            memberPO.setMemberRole(Objects.equals(ownerId, x.getUserId()) ? GroupMemberRoleEnum.OWNER : GroupMemberRoleEnum.MEMBER);
            memberPO.setJoinTime(new Date());
            memberPO.setJoinType(GroupMemberJoinTypeEnum.INVITE);
            memberPO.setIsMuted(false);
            memberPO.setMuteEndTime(new Date());
            memberPO.setLastReadMsgId(0L);
            memberPO.setMuteNotify(false);
            memberPO.setRemark("");
            memberPO.setQuit(false);
            memberPO.setCreateTime(new Date());
            memberPO.setUpdateTime(new Date());
            return memberPO;
        }).collect(Collectors.toList());

        groupMemberRepository.saveBatch(members);

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    获取用户加入的所有群
     */
    @Override
    public DeveloperResult<List<GroupMemberPO>> findJoinGroupList(Long userId) {
        List<GroupMemberPO> groupMembers = groupMemberRepository.findByUserId(userId);
        return DeveloperResult.success(SerialNoHolder.getSerialNo(),groupMembers);
    }

    /*
    修改群成员信息
     */
    @Override
    public DeveloperResult<Boolean> modifyGroupMemberInfo(ModifyGroupMemberInfoDTO dto) {
        GroupMemberPO memberInfo = groupMemberRepository.findByGroupIdAndUserId(dto.getGroupId(), dto.getMemberUserId());
        if(ObjectUtil.isEmpty(memberInfo)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"成员信息不存在");
        }

        memberInfo.setAlias(dto.getAlias() == null ? memberInfo.getAlias() : dto.getAlias());
        memberInfo.setMemberRole(dto.getMemberRole() == null ? memberInfo.getMemberRole() : dto.getMemberRole());
        memberInfo.setIsMuted(dto.getIsMuted() == null ? memberInfo.getIsMuted() : dto.getIsMuted());
        memberInfo.setMuteEndTime(dto.getMuteEndTime() == null ? memberInfo.getMuteEndTime() : dto.getMuteEndTime());
        memberInfo.setLastReadMsgId(dto.getLastReadMsgId() == null ? memberInfo.getLastReadMsgId() : dto.getLastReadMsgId());
        memberInfo.setMuteNotify(dto.getMuteNotify() == null ? memberInfo.getMuteNotify() : dto.getMuteNotify());
        memberInfo.setQuit(dto.getQuit() == null ? memberInfo.getQuit() : dto.getQuit());
        memberInfo.setUpdateTime(new Date());

        groupMemberRepository.updateById(memberInfo);

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
