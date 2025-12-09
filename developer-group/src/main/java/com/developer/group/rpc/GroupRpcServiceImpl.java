package com.developer.group.rpc;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.group.dto.SelfJoinGroupInfoDTO;
import com.developer.group.pojo.GroupMemberPO;
import com.developer.group.service.GroupMemberService;
import com.developer.group.service.GroupService;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;
import com.developer.rpc.dto.group.response.GroupMemberResponseRpcDTO;
import com.developer.rpc.service.group.GroupRpcService;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
@AllArgsConstructor
public class GroupRpcServiceImpl implements GroupRpcService {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    @Override
    public DeveloperResult<List<GroupInfoResponseRpcDTO>> getSelfJoinAllGroupInfo() {
        DeveloperResult<List<SelfJoinGroupInfoDTO>> result = groupService.findSelfJoinAllGroupInfo();
        List<GroupInfoResponseRpcDTO> list = new ArrayList<>();
        if (result.getData() != null) {
            for (SelfJoinGroupInfoDTO selfJoinGroupInfoDTO : result.getData()) {
                GroupInfoResponseRpcDTO dto = new GroupInfoResponseRpcDTO();
                BeanUtils.copyProperties(selfJoinGroupInfoDTO, dto);
                list.add(dto);
            }
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }

    @Override
    public DeveloperResult<List<GroupMemberResponseRpcDTO>> findGroupMemberList(Long groupId) {
        DeveloperResult<List<GroupMemberPO>> groupMember = groupMemberService.findGroupMember(groupId);
        List<GroupMemberResponseRpcDTO> list = groupMember.getData().stream().map(x -> {
            GroupMemberResponseRpcDTO dto = new GroupMemberResponseRpcDTO();
            dto.setGroupId(groupId);
            dto.setMemberUserId(x.getUserId());
            dto.setMemberUserName(x.getAlias());
            return dto;
        }).collect(Collectors.toList());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(),list);
    }
}
