package com.developer.rpc.service.user;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.DTO.user.UserInfoRpcDTO;

import java.util.List;

public interface UserRpcService {

    /*
    获取用户信息
     */
    DeveloperResult<List<UserInfoRpcDTO>> findUserInfo(List<Long> userIdList);

}
