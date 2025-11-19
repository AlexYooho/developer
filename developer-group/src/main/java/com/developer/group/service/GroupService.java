package com.developer.group.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.*;
import com.developer.group.dto.group.CreateGroupRequestDTO;
import com.developer.group.dto.group.CreateGroupResponseDTO;
import com.developer.group.dto.group.ModifyGroupInfoRequestDTO;

import java.util.List;

public interface GroupService {

    /**
     * 创建群聊
     * @return
     */
    DeveloperResult<CreateGroupResponseDTO> createGroup(CreateGroupRequestDTO dto);

    /**
     * 修改群聊
     * @param req
     * @return
     */
    DeveloperResult<Boolean> modifyGroup(ModifyGroupInfoRequestDTO req);

    /**
     * 解散群聊
     * @param req
     * @return
     */
    DeveloperResult<Boolean> deleteGroup(DissolveGroupRequestDTO req);

    /**
     * 查找群聊
     * @param req
     * @return
     */
    DeveloperResult<GroupInfoDTO> findById(FindGroupRequestDTO req);

    /**
     * 查询群聊列表
     * @return
     */
    DeveloperResult<List<GroupInfoDTO>> findGroupList();

    /**
     * 邀请进群
     * @return
     */
    DeveloperResult<Boolean> invite(GroupInviteRequestDTO req);

    /**
     * 获取群成员信息列表
     * @param req
     * @return
     */
    DeveloperResult<List<GroupMemberDTO>> findGroupMembers(FindGroupMembersRequestDTO req);

    /**
     * 退出群聊
     * @param req
     * @return
     */
    DeveloperResult<Boolean> quitGroup(QuitGroupRequestDTO req);

    /**
     * 踢出群聊
     * @param req
     * @return
     */
    DeveloperResult<Boolean> kickGroup(KickOutGroupRequestDTO req);

    /**
     * 获取当前用户所加入的群信息
     * @return
     */
    DeveloperResult<List<SelfJoinGroupInfoDTO>> findSelfJoinAllGroupInfo();

    /*
    校验群主管理员信息
     */
    DeveloperResult<Boolean> verifyChatroomOwnerInfo(Long groupId,Long userId);

}
