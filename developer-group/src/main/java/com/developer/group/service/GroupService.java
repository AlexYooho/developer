package com.developer.group.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.CreateGroupRequestDTO;
import com.developer.group.dto.GroupInviteRequestDTO;

public interface GroupService {

    /**
     * 创建群聊
     * @return
     */
    DeveloperResult createGroup(CreateGroupRequestDTO dto);

    /**
     * 修改群聊
     * @param req
     * @return
     */
    DeveloperResult modifyGroup(CreateGroupRequestDTO req);

    /**
     * 解散群聊
     * @param groupId
     * @return
     */
    DeveloperResult deleteGroup(Long groupId);

    /**
     * 查找群聊
     * @param groupId
     * @return
     */
    DeveloperResult findById(Long groupId);

    /**
     * 查询群聊列表
     * @return
     */
    DeveloperResult findGroupList();

    /**
     * 邀请进群
     * @return
     */
    DeveloperResult invite(GroupInviteRequestDTO req);

    /**
     * 查找群成员
     * @param groupId
     * @return
     */
    DeveloperResult findGroupMembers(Long groupId);

    /**
     * 退出群聊
     * @param groupId
     * @return
     */
    DeveloperResult quitGroup(Long groupId);

    /**
     * 踢出群聊
     * @param groupId
     * @param userId
     * @return
     */
    DeveloperResult kickGroup(Long groupId,Long userId);

}
