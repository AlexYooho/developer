package com.developer.group.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.SelfJoinGroupInfoDTO;

import java.util.List;

public interface GroupMemberService {

    DeveloperResult findGroupMember(Long groupId);

    DeveloperResult batchModifyGroupMemberInfo(List<SelfJoinGroupInfoDTO> list);

}
