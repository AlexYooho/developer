package com.developer.friend.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.*;

import java.util.List;

public interface FriendService {

    /**
     * 获取好友列表
     * @return
     */
    DeveloperResult<List<FriendInfoDTO>> findFriendList();

    /**
     * 是否是好友
     * @return
     */
    DeveloperResult<FriendInfoDTO> isFriend(IsFriendDto dto);

    /**
     *  通过用户id查询好友
     * @param friendId
     * @return
     */
    DeveloperResult<FriendInfoDTO> findFriend(Long friendId);

    /**
     * 发送添加好友请求
     * @param req
     * @return
     */
    DeveloperResult<Boolean> apply(SendAddFriendInfoRequestDTO req);

    /**
     * 同意好友申请
     * @param friendId
     * @param dto
     * @return
     */
    DeveloperResult<Boolean> applyAccept(Long friendId,FriendApplyAcceptDTO dto);

    /**
     * 拒绝好友申请
     * @param friendId
     * @param dto
     * @return
     */
    DeveloperResult<Boolean> applyReject(Long friendId,FriendApplyRejectDTO dto);

    /**
     * 通过好友id删除好友
     * @param friendId
     * @return
     */
    DeveloperResult<Boolean> deleteFriend(Long friendId);

    /**
     * 获取好友添加数
     * @return
     */
    DeveloperResult<Integer> findFriendAddRequestCount();

    /**
     * 获取新好友添加列表
     * @return
     */
    DeveloperResult<List<NewFriendListDTO>> findNewFriendList();

    /**
     * 更新好友添加请求状态
     * @return
     */
    DeveloperResult<Boolean> updateAddFriendRecordStatus();

    /**
     * 批量修改好友信息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> batchModifyFriendInfo(BatchModifyFriendListRequestDTO req);
}
