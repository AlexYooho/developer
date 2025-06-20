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
     * @param req
     * @return
     */
    DeveloperResult<FriendInfoDTO> findFriend(FindFriendRequestDTO req);

    /**
     * 发送添加好友请求
     * @param req
     * @return
     */
    DeveloperResult<Boolean> sendAddFriendRequest(SendAddFriendInfoRequestDTO req);

    /**
     * 处理好友请求
     * @param req
     * @return
     */
    DeveloperResult<Boolean> processFriendRequest(ProcessAddFriendRequestDTO req);

    /**
     * 通过好友id删除好友
     * @param req
     * @return
     */
    DeveloperResult<Boolean> deleteFriendByFriendId(DeleteFriendRequestDTO req);

    /**
     * 获取好友添加数
     * @return
     */
    DeveloperResult<Integer> findFriendAddRequestCount();

    /**
     * 获取新好友添加列表
     * @return
     */
    DeveloperResult<List<NewFriendListDTO>> findNewFriendList(String serialNo);

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
    DeveloperResult<Boolean> modifyFriendList(BatchModifyFriendListRequestDTO req);
}
