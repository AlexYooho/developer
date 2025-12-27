package com.developer.rpc.service.group;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;
import com.developer.rpc.dto.group.response.GroupMemberResponseRpcDTO;
import com.developer.rpc.dto.group.response.SameGroupInfoResponseRpcDTO;

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

    /*
    获取当前登录用户和目标用户相同群信息
     */
    DeveloperResult<List<SameGroupInfoResponseRpcDTO>> findSameGroupInfoList(Long targetId);

}
