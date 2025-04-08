package com.developer.group.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.BatchModifyGroupMemberInfoRequestDTO;
import com.developer.group.dto.FindGroupMemberUserIdRequestDTO;
import com.developer.group.dto.SelfJoinGroupInfoDTO;

import java.util.List;

public interface GroupMemberService {

    DeveloperResult<List<Long>> findGroupMember(FindGroupMemberUserIdRequestDTO req);

    DeveloperResult<Boolean> batchModifyGroupMemberInfo(BatchModifyGroupMemberInfoRequestDTO req);

}
