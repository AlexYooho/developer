package com.developer.friend.rpc;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.friend.dto.FriendInfoDTO;
import com.developer.friend.service.FriendService;
import com.developer.rpc.dto.friend.response.FriendInfoResponseRpcDTO;
import com.developer.rpc.service.friend.FriendRpcService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@RequiredArgsConstructor
public class FriendRpcProviderService implements FriendRpcService {

    private final FriendService friendService;

    @Override
    public DeveloperResult<List<FriendInfoResponseRpcDTO>> findFriends() {
        DeveloperResult<List<FriendInfoDTO>> result = friendService.findFriendList();
        if(!result.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),result.getMsg());
        }

        List<FriendInfoResponseRpcDTO> list = result.getData().stream().map(x -> {
            FriendInfoResponseRpcDTO dto = new FriendInfoResponseRpcDTO();
            dto.setId(x.getId());
            dto.setAlias(x.getAlias());
            dto.setTagName(x.getTagName());
            dto.setAccount(x.getAccount());
            dto.setNickName(x.getNickName());
            dto.setHeadImage(x.getHeadImage());
            dto.setHeadImageThumb(x.getHeadImageThumb());
            dto.setArea(x.getArea());
            dto.setUserName(x.getUserName());
            dto.setSex(x.getSex());
            return dto;
        }).collect(Collectors.toList());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(),list);
    }
}
