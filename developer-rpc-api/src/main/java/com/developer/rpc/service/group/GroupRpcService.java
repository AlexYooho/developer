package com.developer.rpc.service.group;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;

import java.util.List;

public interface GroupRpcService {

    DeveloperResult<List<GroupInfoResponseRpcDTO>> getSelfJoinAllGroupInfo();

}
