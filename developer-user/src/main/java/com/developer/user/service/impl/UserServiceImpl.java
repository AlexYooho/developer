package com.developer.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.SelfUserInfoModel;
import com.developer.framework.utils.BeanUtils;
import com.developer.user.dto.ModifyUserInfoDTO;
import com.developer.user.dto.UserInfoDTO;
import com.developer.user.dto.UserRegisterDTO;
import com.developer.user.pojo.UserPO;
import com.developer.user.repository.UserRepository;
import com.developer.user.service.UserService;
import com.developer.framework.model.DeveloperResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;


    /**
     * 用户注册
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult register(UserRegisterDTO dto) {
        if("".equals(dto.getAccount())){
            return DeveloperResult.error(500,"请输入正确的手机号");
        }

        if("".equals(dto.getPassword())){
            return DeveloperResult.error(500,"请输入密码");
        }

        if("".equals(dto.getNickname())){
            return DeveloperResult.error(500,"请输入昵称");
        }

        UserPO userPO = userRepository.findByAccount(dto.getAccount());
        if(userPO!=null){
            return DeveloperResult.error(500,"手机号已存在,请重新输入");
        }

        userPO = new UserPO(dto.getAccount(), "", dto.getNickname(), "","", dto.getPassword(), dto.getSex(),0,"",new Date(),new Date());
        userRepository.save(userPO);
        return DeveloperResult.success();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public DeveloperResult findSelfUserInfo() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserPO user = userRepository.getById(userId);
        UserInfoDTO userInfoDTO = BeanUtils.copyProperties(user, UserInfoDTO.class);
        return DeveloperResult.success(userInfoDTO);
    }

    /**
     * 根据userId查找用户信息
     * @param userId
     * @return
     */
    @Override
    public DeveloperResult findUserInfoById(Long userId) {
        UserPO user = userRepository.getById(userId);
        UserInfoDTO userInfoDTO = BeanUtils.copyProperties(user, UserInfoDTO.class);
        // 设置在线状态
        userInfoDTO.setOnline(true);
        return DeveloperResult.success(userInfoDTO);
    }

    /**
     * 通过名字查询用户信息
     * @param name
     * @return
     */
    @Override
    public DeveloperResult findUserByName(String name) {
        List<UserPO> userInfos = userRepository.findByName(name);
        List<Long> userIds = userInfos.stream().map(UserPO::getId).collect(Collectors.toList());
        // TODO 获取在线客户端
        //List<Long> onlineUserIds = imOnlineTools.getOnlineUser(userIds);
        List<Long> onlineUserIds = new ArrayList<>();
        List<UserInfoDTO> collect = userInfos.stream().map(x -> {
            UserInfoDTO userInfoDTO = BeanUtils.copyProperties(x, UserInfoDTO.class);
            userInfoDTO.setOnline(onlineUserIds.contains(x.getId()));
            return userInfoDTO;
        }).collect(Collectors.toList());
        return DeveloperResult.success(collect);
    }

    @Override
    public DeveloperResult modifyUserInfo(ModifyUserInfoDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        if(!userId.equals(dto.getId())){
            return DeveloperResult.error("不允许修改其他用户信息");
        }

        UserPO user = userRepository.getById(userId);
        if(user==null){
            return DeveloperResult.error("用户不存在");
        }

        // TODO 跨服务更新所在好友，群当前用户信息
        // 更新自己好友列表中的昵称和头像
//        if(!user.getNickname().equals(dto.getNickname()) || !user.getHeadImageThumb().equals(dto.getHeadImageThumb())){
//            List<Friend> friends = friendRepository.findFriendByFriendId(userId);
//            for (Friend friend:friends){
//                friend.setFriendNickName(dto.getNickname());
//                friend.setFriendHeadImage(dto.getHeadImage());
//            }
//            friendRepository.updateBatchById(friends);
//        }
//
//        // 更新所在群的头像
//        if(!user.getHeadImage().equals(dto.getHeadImage())){
//            List<GroupMember> members = groupMemberRepository.findByUserId(userId);
//            for (GroupMember member : members) {
//                member.setHeadImage(dto.getHeadImage());
//            }
//            groupMemberRepository.updateBatchById(members);
//        }

        user.setNickname(dto.getNickname());
        user.setSex(dto.getSex());
        user.setSignature(dto.getSignature());
        user.setHeadImage(dto.getHeadImage());
        user.setHeadImageThumb(dto.getHeadImageThumb());
        this.userRepository.updateById(user);
        return DeveloperResult.success("修改信息成功");
    }


}
