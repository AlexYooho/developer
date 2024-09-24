package com.developer.group.service.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.SelfJoinGroupInfoDTO;
import com.developer.group.pojo.GroupMemberPO;
import com.developer.group.repository.GroupMemberRepository;
import com.developer.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Override
    public DeveloperResult<List<Long>> findGroupMember(Long groupId) {
        List<GroupMemberPO> members = this.groupMemberRepository.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMemberPO::getUserId).collect(Collectors.toList());
        return DeveloperResult.success(userIds);
    }

    @Override
    public DeveloperResult<Boolean> batchModifyGroupMemberInfo(List<SelfJoinGroupInfoDTO> list) {

        List<GroupMemberPO> ll = new ArrayList<>();
        this.groupMemberRepository.updateBatchById(ll);

        return DeveloperResult.success();
    }
}
