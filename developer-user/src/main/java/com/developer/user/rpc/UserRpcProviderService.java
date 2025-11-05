package com.developer.user.rpc;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.rpc.DTO.user.request.UserInfoRequestRpcDTO;
import com.developer.rpc.DTO.user.response.UserInfoResponseRpcDTO;
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
    public DeveloperResult<List<UserInfoResponseRpcDTO>> findUserInfo(UserInfoRequestRpcDTO request) {
        DeveloperResult<List<UserInfoDTO>> userInfoResult = userService.findUserInfoByUserId(request.getUserIds());
        if(!userInfoResult.getIsSuccessful()){
            return DeveloperResult.error(userInfoResult.getSerialNo(),userInfoResult.getMsg());
        }

        List<UserInfoResponseRpcDTO> userInfoList = userInfoResult.getData().stream().map(x -> {
            UserInfoResponseRpcDTO dto = new UserInfoResponseRpcDTO();
            dto.setUserId(x.getId());
            dto.setAccount(x.getAccount());
            dto.setUserName(x.getUsername());
            dto.setNickName(x.getNickname());
            dto.setAvatar(x.getHeadImage());
            dto.setAvatarThumb(x.getHeadImageThumb());
            dto.setSex(x.getSex());
            dto.setArea(x.getArea());
            dto.setSignature(x.getSignature());
            dto.setLastLoginTime(x.getLastLoginTime());
            return dto;
        }).collect(Collectors.toList());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(),userInfoList);
    }
}
