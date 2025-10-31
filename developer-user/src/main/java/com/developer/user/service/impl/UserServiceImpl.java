package com.developer.user.service.impl;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.utils.*;
import com.developer.user.client.FriendClient;
import com.developer.user.client.GroupMemberClient;
import com.developer.user.dto.*;
import com.developer.user.pojo.UserPO;
import com.developer.user.repository.UserRepository;
import com.developer.user.service.UserService;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

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

    @Autowired
    private VerifyCodeService verifyCodeService;

    /**
     * 用户注册
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> register(UserRegisterDTO dto) {
        String serialNo = SerialNoHolder.getSerialNo();
        if("".equals(dto.getAccount())){
            return DeveloperResult.error(serialNo,500,"请输入正确的手机号");
        }

        if(!mailUtil.verifyEmailAddress(dto.getEmail())){
            return DeveloperResult.error(serialNo,500,"请输入正确的邮箱");
        }

        if("".equals(dto.getPassword())){
            return DeveloperResult.error(serialNo,500,"请输入密码");
        }

        if("".equals(dto.getNickname())){
            return DeveloperResult.error(serialNo,500,"请输入昵称");
        }

        if(dto.getVerifyCode()==null){
            return DeveloperResult.error(serialNo,500,"请输入验证码");
        }

        Long existCount = userRepository.findByEmail(dto.getEmail());
        if(existCount>0){
            return DeveloperResult.error(serialNo,500,"邮箱已存在,请重新输入");
        }

        UserPO userPO = userRepository.findByAccount(dto.getAccount());
        if(userPO!=null){
            return DeveloperResult.error(serialNo,500,"手机号已存在,请重新输入");
        }

        DeveloperResult<Boolean> checkResult = verifyCodeService.checkVerifyCode(serialNo,VerifyCodeTypeEnum.REGISTER_ACCOUNT,dto.getEmail(),dto.getVerifyCode());
        if(!checkResult.getIsSuccessful()){
            return DeveloperResult.error(serialNo,checkResult.getMsg());
        }

        userPO = new UserPO(dto.getAccount(), "", dto.getNickname(), "","", passwordEncoder.encode(dto.getPassword()), dto.getSex(),0, dto.getEmail(), "",new Date(),new Date());
        userRepository.save(userPO);

        return DeveloperResult.success(serialNo);
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public DeveloperResult<UserInfoDTO> findSelfUserInfo() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        UserPO user = userRepository.getById(userId);
        UserInfoDTO userInfoDTO = BeanUtils.copyProperties(user, UserInfoDTO.class);
        return DeveloperResult.success(serialNo,userInfoDTO);
    }

    /**
     * 根据userId查找用户信息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<UserInfoDTO> findUserInfoById(FindUserRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        UserPO user = userRepository.getById(req.getUserId());
        UserInfoDTO userInfoDTO = BeanUtils.copyProperties(user, UserInfoDTO.class);
        // 设置在线状态
        userInfoDTO.setOnline(true);
        return DeveloperResult.success(serialNo,userInfoDTO);
    }

    /**
     * 通过名字查询用户信息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<List<UserInfoDTO>> findUserByName(FindUserRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        List<UserPO> userInfos = userRepository.findByName(req.getUserName());
        List<Long> userIds = userInfos.stream().map(UserPO::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imOnlineUtil.getOnlineUser(userIds);
        List<UserInfoDTO> collect = userInfos.stream().map(x -> {
            UserInfoDTO userInfoDTO = BeanUtils.copyProperties(x, UserInfoDTO.class);
            userInfoDTO.setOnline(onlineUserIds.contains(x.getId()));
            return userInfoDTO;
        }).collect(Collectors.toList());
        return DeveloperResult.success(serialNo,collect);
    }

    @Override
    public DeveloperResult<Boolean> modifyUserInfo(ModifyUserInfoDTO dto) {
        String serialNo = SerialNoHolder.getSerialNo();
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        if(!userId.equals(dto.getId())){
            return DeveloperResult.error(serialNo,"不允许修改其他用户信息");
        }

        UserPO user = userRepository.getById(userId);
        if(user==null){
            return DeveloperResult.error(serialNo,"用户不存在");
        }

        // 更新自己好友列表中的昵称和头像
        if(!user.getNickname().equals(dto.getNickname()) || !user.getHeadImageThumb().equals(dto.getHeadImageThumb())){
            DeveloperResult<List<FriendInfoDTO>> friends = friendClient.friends(serialNo);
            List<FriendInfoDTO> list2 = friends.getData();
            for (FriendInfoDTO friend:list2){
                friend.setNickName(dto.getNickname());
                friend.setHeadImage(dto.getHeadImage());
            }
            friendClient.modifyFriend(BatchModifyFriendListRequestDTO.builder().list(list2).serialNo(serialNo).build());
        }

        // 更新所在群的头像
        if(!user.getHeadImage().equals(dto.getHeadImage())){
            DeveloperResult<List<SelfJoinGroupInfoDTO>> selfJoinAllGroupInfo = groupMemberClient.getSelfJoinAllGroupInfo(serialNo);
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
        return DeveloperResult.success(serialNo);
    }

    /**
     * 获取用户在线终端
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<List<OnlineTerminalDTO>> findOnlineTerminal(FindOnlineTerminalRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        List<Long> userIdList = Arrays.stream(req.getUserIds().split(",")).map(Long::parseLong).collect(Collectors.toList());
        Map<Long, List<MessageTerminalTypeEnum>> onlineTerminals = imOnlineUtil.getOnlineTerminal(userIdList);
        List<OnlineTerminalDTO> list=new LinkedList<>();
        onlineTerminals.forEach((userId,terminals)->{
            List<Integer> collect = terminals.stream().map(MessageTerminalTypeEnum::code).collect(Collectors.toList());
            list.add(new OnlineTerminalDTO(userId,collect));
        });
        return DeveloperResult.success(serialNo,list);
    }

    /**
     * 修改密码
     */
    @Override
    public DeveloperResult<Boolean> modifyUserPassword(ModifyUserPasswordDTO dto) {
        String serialNo = SerialNoHolder.getSerialNo();
        if(dto.getOldPassword().isEmpty()){
            return DeveloperResult.error(serialNo,"请输入原始密码");
        }

        if(dto.getNewPassword().isEmpty()){
            return DeveloperResult.error(serialNo,"请输入新密码");
        }

        if(dto.getVerifyCode()==null){
            return DeveloperResult.error(serialNo,"请输入验证码");
        }

        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserPO user = userRepository.getById(userId);
        if(user==null){
            return DeveloperResult.error(serialNo,"用户不存在");
        }

        DeveloperResult<Boolean> checkResult = verifyCodeService.checkVerifyCode(serialNo,VerifyCodeTypeEnum.MODIFY_PASSWORD,user.getEmail(),dto.getVerifyCode());
        if(!checkResult.getIsSuccessful()){
            return DeveloperResult.error(serialNo,checkResult.getMsg());
        }

        if(passwordEncoder.matches(dto.getOldPassword(),user.getPassword())){
            return DeveloperResult.error(serialNo,"原始密码错误");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.userRepository.updateById(user);

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<List<UserInfoDTO>> findUserInfoByUserId(List<Long> userIdList) {
        List<UserPO> userList = userRepository.findByUserId(userIdList);
        List<UserInfoDTO> list = userList.stream().map(x -> {
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setId(x.getId());
            userInfoDTO.setAccount(x.getAccount());
            userInfoDTO.setArea(x.getArea());
            return userInfoDTO;
        }).collect(Collectors.toList());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(),list);
    }
}
