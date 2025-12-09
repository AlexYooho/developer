package com.developer.rpc.service.group;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;
import com.developer.rpc.dto.group.response.GroupMemberResponseRpcDTO;

import java.util.List;

public interface GroupRpcService {

    /*
    获取所加入的群
     */
    DeveloperResult<List<GroupInfoResponseRpcDTO>> getSelfJoinAllGroupInfo();

    /*
    获取群成员
     */
    DeveloperResult<List<GroupMemberResponseRpcDTO>> findGroupMemberList(Long groupId);

}
