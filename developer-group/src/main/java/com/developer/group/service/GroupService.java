package com.developer.group.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.*;

import java.util.List;

public interface GroupService {

    /**
     * 创建群聊
     * @return
     */
    DeveloperResult<CreateGroupRequestDTO> createGroup(CreateGroupRequestDTO dto);

    /**
     * 修改群聊
     * @param req
     * @return
     */
    DeveloperResult<CreateGroupRequestDTO> modifyGroup(CreateGroupRequestDTO req);

    /**
     * 解散群聊
     * @param groupId
     * @return
     */
    DeveloperResult<Boolean> deleteGroup(Long groupId);

    /**
     * 查找群聊
     * @param groupId
     * @return
     */
    DeveloperResult<GroupInfoDTO> findById(Long groupId);

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
     * 查找群成员
     * @param groupId
     * @return
     */
    DeveloperResult<List<GroupMemberDTO>> findGroupMembers(Long groupId);

    /**
     * 退出群聊
     * @param groupId
     * @return
     */
    DeveloperResult<Boolean> quitGroup(Long groupId);

    /**
     * 踢出群聊
     * @param groupId
     * @param userId
     * @return
     */
    DeveloperResult<Boolean> kickGroup(Long groupId,Long userId);

    /**
     * 获取当前用户所加入的群信息
     * @return
     */
    DeveloperResult<List<SelfJoinGroupInfoDTO>> findSelfJoinAllGroupInfo();

}
