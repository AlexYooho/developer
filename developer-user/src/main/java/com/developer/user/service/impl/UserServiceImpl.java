package com.developer.user.service.impl;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.IMOnlineUtil;
import com.developer.framework.utils.MailUtil;
import com.developer.framework.utils.RedisUtil;
import com.developer.user.client.FriendClient;
import com.developer.user.client.GroupMemberClient;
import com.developer.user.dto.*;
import com.developer.user.pojo.UserPO;
import com.developer.user.repository.UserRepository;
import com.developer.user.service.UserService;
import com.developer.framework.model.DeveloperResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IMOnlineUtil imOnlineUtil;

    @Autowired
    private FriendClient friendClient;

    @Autowired
    private GroupMemberClient groupMemberClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailUtil mailUtil;

    /**
     * 用户注册
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> register(UserRegisterDTO dto) {
        if("".equals(dto.getAccount())){
            return DeveloperResult.error(500,"请输入正确的手机号");
        }

        if(!mailUtil.verifyEmailAddress(dto.getEmail())){
            return DeveloperResult.error(500,"请输入正确的邮箱");
        }

        if("".equals(dto.getPassword())){
            return DeveloperResult.error(500,"请输入密码");
        }

        if("".equals(dto.getNickname())){
            return DeveloperResult.error(500,"请输入昵称");
        }

        if(dto.getVerifyCode()==null){
            return DeveloperResult.error(500,"请输入验证码");
        }

        String key = RedisKeyConstant.RegisterVerifyCode(dto.getEmail());
        Integer verifyCode = redisUtil.get(key, Integer.class);
        if(!Objects.equals(verifyCode, dto.getVerifyCode())){
            return DeveloperResult.error(500,"验证码错误");
        }

        UserPO userPO = userRepository.findByAccount(dto.getAccount());
        if(userPO!=null){
            return DeveloperResult.error(500,"手机号已存在,请重新输入");
        }

        userPO = new UserPO(dto.getAccount(), "", dto.getNickname(), "","", passwordEncoder.encode(dto.getPassword()), dto.getSex(),0, dto.getEmail(), "",new Date(),new Date());
        userRepository.save(userPO);
        return DeveloperResult.success();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public DeveloperResult<UserInfoDTO> findSelfUserInfo() {
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
    public DeveloperResult<UserInfoDTO> findUserInfoById(Long userId) {
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
    public DeveloperResult<List<UserInfoDTO>> findUserByName(String name) {
        List<UserPO> userInfos = userRepository.findByName(name);
        List<Long> userIds = userInfos.stream().map(UserPO::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imOnlineUtil.getOnlineUser(userIds);
        List<UserInfoDTO> collect = userInfos.stream().map(x -> {
            UserInfoDTO userInfoDTO = BeanUtils.copyProperties(x, UserInfoDTO.class);
            userInfoDTO.setOnline(onlineUserIds.contains(x.getId()));
            return userInfoDTO;
        }).collect(Collectors.toList());
        return DeveloperResult.success(collect);
    }

    @Override
    public DeveloperResult<Boolean> modifyUserInfo(ModifyUserInfoDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        if(!userId.equals(dto.getId())){
            return DeveloperResult.error("不允许修改其他用户信息");
        }

        UserPO user = userRepository.getById(userId);
        if(user==null){
            return DeveloperResult.error("用户不存在");
        }

        // 更新自己好友列表中的昵称和头像
        if(!user.getNickname().equals(dto.getNickname()) || !user.getHeadImageThumb().equals(dto.getHeadImageThumb())){
            DeveloperResult<List<FriendInfoDTO>> friends = friendClient.friends();
            List<FriendInfoDTO> list2 = friends.getData();
            for (FriendInfoDTO friend:list2){
                friend.setNickName(dto.getNickname());
                friend.setHeadImage(dto.getHeadImage());
            }
            friendClient.modifyFriend(list2);
        }

        // 更新所在群的头像
        if(!user.getHeadImage().equals(dto.getHeadImage())){
            DeveloperResult<List<SelfJoinGroupInfoDTO>> selfJoinAllGroupInfo = groupMemberClient.getSelfJoinAllGroupInfo();
            List<SelfJoinGroupInfoDTO> joinGroupInfoList = selfJoinAllGroupInfo.getData();
            for (SelfJoinGroupInfoDTO member : joinGroupInfoList) {
                member.setHeadImage(dto.getHeadImage());
            }
            groupMemberClient.batchModifyGroupMemberInfo(joinGroupInfoList);
        }

        user.setNickname(dto.getNickname());
        user.setSex(dto.getSex());
        user.setSignature(dto.getSignature());
        user.setHeadImage(dto.getHeadImage());
        user.setHeadImageThumb(dto.getHeadImageThumb());
        this.userRepository.updateById(user);
        return DeveloperResult.success();
    }

    /**
     * 获取用户在线终端
     * @param userIds
     * @return
     */
    @Override
    public DeveloperResult<List<OnlineTerminalDTO>> findOnlineTerminal(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        Map<Long, List<MessageTerminalTypeEnum>> onlineTerminals = imOnlineUtil.getOnlineTerminal(userIdList);
        List<OnlineTerminalDTO> list=new LinkedList<>();
        onlineTerminals.forEach((userId,terminals)->{
            List<Integer> collect = terminals.stream().map(MessageTerminalTypeEnum::code).collect(Collectors.toList());
            list.add(new OnlineTerminalDTO(userId,collect));
        });
        return DeveloperResult.success(list);
    }

    @Override
    public DeveloperResult<Integer> sendRegisterVerifyCode(String emailAccount) {
        if(!mailUtil.verifyEmailAddress(emailAccount)){
            return DeveloperResult.error(500,"请输入正确的邮箱");
        }

        Integer code = mailUtil.sendAuthorizationCode();
        String key = RedisKeyConstant.RegisterVerifyCode(emailAccount);
        redisUtil.set(key,code,5, TimeUnit.MINUTES);

        return DeveloperResult.success();
    }

}
