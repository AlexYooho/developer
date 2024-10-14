package com.developer.friend.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.FriendInfoDTO;
import com.developer.friend.dto.NewFriendListDTO;
import com.developer.friend.dto.ProcessAddFriendRequestDTO;
import com.developer.friend.dto.SendAddFriendInfoRequestDTO;
import com.developer.friend.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("friend")
@RestController
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 好友列表
     * @return
     */
    @GetMapping("/list")
    public DeveloperResult<List<FriendInfoDTO>> friends(){
        return friendService.findFriendList();
    }

    /**
     * 通过好友id查询好友信息
     * @param friendId
     * @return
     */
    @GetMapping("/find/{friendId}")
    public DeveloperResult<FriendInfoDTO> findFriend(@PathVariable("friendId") Long friendId){
        return friendService.findFriend(friendId);
    }

    /**
     * 通过好友id删除好友
     * @param friendId
     * @return
     */
    @DeleteMapping("/delete/{friendId}")
    public DeveloperResult<Boolean> deleteFriend(@PathVariable("friendId") Long friendId){
        return friendService.deleteFriendByFriendId(friendId);
    }

    /**
     * 更新好友信息
     * @return
     */
    @PutMapping("/update")
    public DeveloperResult<Boolean> modifyFriend(){
        return DeveloperResult.success();
    }

    /**
     * 批量更新好友信息
     * @param list
     * @return
     */
    @PutMapping("/update/list")
    public DeveloperResult<Boolean> modifyFriendList(@RequestBody List<FriendInfoDTO> list){
        return friendService.modifyFriendList(list);
    }

    /**
     * 发送添加好友请求
     * @param req
     * @return
     */
    @PostMapping("/sendAddFriendRequest")
    public DeveloperResult<Boolean> sendAddFriendRequest(@RequestBody SendAddFriendInfoRequestDTO req){
        return friendService.sendAddFriendRequest(req);
    }

    /**
     * 处理好友请求
     * @param req
     * @return
     */
    @PostMapping("/processFriendRequest")
    public DeveloperResult<Boolean> processFriendRequest(@RequestBody ProcessAddFriendRequestDTO req){
        return friendService.processFriendRequest(req);
    }

    /**
     * 好友添加请求数
     * @return
     */
    @GetMapping("/friendAddRequestCount")
    public DeveloperResult<Integer> friendAddRequestCount(){
        return friendService.findFriendAddRequestCount();
    }

    /**
     * 新朋友列表
     * @return
     */
    @GetMapping("/new/list")
    public DeveloperResult<List<NewFriendListDTO>> newFriendList(){
        return friendService.findNewFriendList();
    }

    /**
     * 修改好友申请状态
     * @return
     */
    @PutMapping("/update/friend/applicant/record/status")
    public DeveloperResult<Boolean> updateAddFriendRecordStatus(){
        return friendService.updateAddFriendRecordStatus();
    }

    /**
     * 是否是好友
     * @param friendId
     * @param userId
     * @return
     */
    @GetMapping("{friendId}/is-friend/{userId}")
    public DeveloperResult<FriendInfoDTO> isFriend(@PathVariable("friendId") Long friendId,@PathVariable("userId") Long userId){
        return friendService.isFriend(userId,friendId);
    }
}
