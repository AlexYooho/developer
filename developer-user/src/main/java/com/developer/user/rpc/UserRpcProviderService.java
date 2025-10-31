package com.developer.user.rpc;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.rpc.DTO.user.UserInfoRpcDTO;
import com.developer.rpc.service.user.UserRpcService;
import com.developer.user.dto.UserInfoDTO;
import com.developer.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Component
@RequiredArgsConstructor
public class UserRpcProviderService implements UserRpcService {

    private final UserService userService;

    @Override
    public DeveloperResult<List<UserInfoRpcDTO>> findUserInfo(List<Long> userIdList) {
        DeveloperResult<List<UserInfoDTO>> userInfoResult = userService.findUserInfoByUserId(userIdList);
        if(!userInfoResult.getIsSuccessful()){
            return DeveloperResult.error(userInfoResult.getSerialNo(),userInfoResult.getMsg());
        }

        List<UserInfoRpcDTO> userInfoList = userInfoResult.getData().stream().map(x -> {
            UserInfoRpcDTO dto = new UserInfoRpcDTO();
            dto.setUserId(x.getId());
            dto.setAccount(x.getAccount());
            dto.setArea(x.getArea());
            return dto;
        }).collect(Collectors.toList());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(),userInfoList);
    }
}
