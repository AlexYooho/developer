package com.developer.group.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.BatchModifyGroupMemberInfoRequestDTO;
import com.developer.group.dto.FindGroupMemberUserIdRequestDTO;
import com.developer.group.dto.ModifyGroupMemberInfoDTO;
import com.developer.group.dto.SelfJoinGroupInfoDTO;
import com.developer.group.pojo.GroupMemberPO;

import java.util.List;

public interface GroupMemberService {

    /*
    获取群成员信息
     */
    DeveloperResult<List<Long>> findGroupMember(FindGroupMemberUserIdRequestDTO req);

    /*
    获取群成员信息
     */
    DeveloperResult<List<GroupMemberPO>> findGroupMember(Long groupId);

    /*
    获取群成员信息
     */
    DeveloperResult<GroupMemberPO> findGroupMemberInfo(Long groupId,Long memberUserId);

    /*
    批量修改群成员信息
     */
    DeveloperResult<Boolean> batchModifyGroupMemberInfo(BatchModifyGroupMemberInfoRequestDTO req);

    /*
    建群初始化群成员
     */
    DeveloperResult<Boolean> createGroupInitMemberInfo(Long groupId,Long ownerId,List<Long> memberIds);

    /*
    获取用户加入的所有群
     */
    DeveloperResult<List<GroupMemberPO>> findJoinGroupList(Long userId);

    /*
    修改群成员信息
     */
    DeveloperResult<Boolean> modifyGroupMemberInfo(ModifyGroupMemberInfoDTO dto);

}
