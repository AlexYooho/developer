package com.developer.friend.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.ProcessAddFriendRequestDTO;
import com.developer.friend.dto.SendAddFriendInfoRequestDTO;

public interface FriendService {

    /**
     * 通过用户id查询好友集合
     * @param userId
     * @return
     */
    DeveloperResult findFriendByUserId(Long userId);

    /**
     * 获取好友列表
     * @return
     */
    DeveloperResult findFriendList();

    /**
     * 是否是好友
     * @param userId1
     * @param userId2
     * @return
     */
    DeveloperResult isFriend(Long userId1,Long userId2);

    /**
     *  通过用户id查询好友
     * @param friendId
     * @return
     */
    DeveloperResult findFriend(Long friendId);

    /**
     * 发送添加好友请求
     * @param req
     * @return
     */
    DeveloperResult sendAddFriendRequest(SendAddFriendInfoRequestDTO req);

    /**
     * 处理好友请求
     * @param req
     * @return
     */
    DeveloperResult processFriendRequest(ProcessAddFriendRequestDTO req);

    /**
     * 通过好友id删除好友
     * @param friendId
     * @return
     */
    DeveloperResult deleteFriendByFriendId(Long friendId);

    /**
     * 获取好友添加数
     * @return
     */
    DeveloperResult findFriendAddRequestCount();

    /**
     * 获取新好友添加列表
     * @return
     */
    DeveloperResult findNewFriendList();

    /**
     * 更新好友添加请求状态
     * @return
     */
    DeveloperResult updateAddFriendRecordStatus();
}