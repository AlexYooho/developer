package com.developer.friend.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.*;
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
    public DeveloperResult<List<FriendInfoDTO>> friendList(@RequestParam("serial_no") String serialNo){
        return friendService.findFriendList(serialNo);
    }

    /**
     * 通过好友id查询好友信息
     * @param req
     * @return
     */
    @GetMapping("/find")
    public DeveloperResult<FriendInfoDTO> findFriend(@RequestBody FindFriendRequestDTO req){
        return friendService.findFriend(req);
    }

    /**
     * 通过好友id删除好友
     * @param req
     * @return
     */
    @DeleteMapping("/delete")
    public DeveloperResult<Boolean> deleteFriend(@RequestBody DeleteFriendRequestDTO req){
        return friendService.deleteFriendByFriendId(req);
    }

    /**
     * 更新好友信息
     * @return
     */
    @PutMapping("/update")
    public DeveloperResult<Boolean> modifyFriend(){
        return DeveloperResult.success("");
    }

    /**
     * 批量更新好友信息
     * @param req
     * @return
     */
    @PutMapping("/update/list")
    public DeveloperResult<Boolean> modifyFriendList(@RequestBody BatchModifyFriendListRequestDTO req){
        return friendService.modifyFriendList(req);
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
    public DeveloperResult<Integer> friendAddRequestCount(@RequestParam("serial_no") String serialNo){
        return friendService.findFriendAddRequestCount(serialNo);
    }

    /**
     * 新朋友列表
     * @return
     */
    @GetMapping("/new/list")
    public DeveloperResult<List<NewFriendListDTO>> newFriendList(@RequestParam("serial_no") String serialNo){
        return friendService.findNewFriendList(serialNo);
    }

    /**
     * 修改好友申请状态
     * @return
     */
    @PutMapping("/update/friend/applicant/record/status")
    public DeveloperResult<Boolean> updateAddFriendRecordStatus(@RequestParam("serial_no") String serialNo){
        return friendService.updateAddFriendRecordStatus(serialNo);
    }

    /**
     * 是否是好友
     * @param req
     * @return
     */
    @GetMapping("is-friend")
    public DeveloperResult<FriendInfoDTO> isFriend(@RequestBody IsFriendDto req){
        return friendService.isFriend(req);
    }
}
