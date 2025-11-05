package com.developer.rpc.service.user;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.DTO.user.request.UserInfoRequestRpcDTO;
import com.developer.rpc.DTO.user.response.UserInfoResponseRpcDTO;

import java.util.List;

public interface UserRpcService {

    /*
    获取用户信息
     */
    DeveloperResult<List<UserInfoResponseRpcDTO>> findUserInfo(UserInfoRequestRpcDTO request);

}
