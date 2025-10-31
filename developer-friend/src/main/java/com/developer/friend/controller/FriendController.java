package com.developer.friend.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.*;
import com.developer.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("friend")
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 好友列表
     * @return
     */
    @GetMapping("/list")
    public DeveloperResult<List<FriendInfoDTO>> friendList(){
        return friendService.findFriendList();
    }

    /**
     * 查询好友信息
     * @param friendId
     * @return
     */
    @GetMapping("/{friend_id}")
    public DeveloperResult<FriendInfoDTO> findFriend(@PathVariable("friend_id") Long friendId){
        return friendService.findFriend(friendId);
    }

    /**
     * 删除好友
     * @param friendId
     * @return
     */
    @DeleteMapping("/{friend_id}")
    public DeveloperResult<Boolean> deleteFriend(@PathVariable("friend_id") Long friendId){
        return friendService.deleteFriend(friendId);
    }

    /**
     * 更新好友信息
     * @return
     */
    @PutMapping("/{friend_id}")
    public DeveloperResult<Boolean> modifyFriend(@PathVariable("friend_id") Long friendId){
        return DeveloperResult.success("");
    }

    /**
     * 批量更新好友信息
     * @param req
     * @return
     */
    @PutMapping("/batch")
    public DeveloperResult<Boolean> batchModifyFriendInfo(@RequestBody BatchModifyFriendListRequestDTO req){
        return friendService.batchModifyFriendInfo(req);
    }

    /**
     * 发送好友请求
     * @param req
     * @return
     */
    @PostMapping("/apply")
    public DeveloperResult<Boolean> apply(@RequestBody SendAddFriendInfoRequestDTO req){
        return friendService.apply(req);
    }

    /**
     * 同意好友请求
     * @param friendId
     * @param dto
     * @return
     */
    @PostMapping("/{friend_id}/apply/accept")
    public DeveloperResult<Boolean> applyAccept(@PathVariable("friend_id") Long friendId,@RequestBody FriendApplyAcceptDTO dto){
        return friendService.applyAccept(friendId,dto);
    }

    /**
     * 拒绝好友请求
     * @param friendId
     * @param dto
     * @return
     */
    @PostMapping("/{friend_id}/apply/reject")
    public DeveloperResult<Boolean> applyReject(@PathVariable("friend_id") Long friendId,@RequestBody FriendApplyRejectDTO dto){
        return friendService.applyReject(friendId,dto);
    }

    /**
     * 待处理好友请求数
     * @return
     */
    @GetMapping("/apply/pending/count")
    public DeveloperResult<Integer> friendAddRequestCount(){
        return friendService.findFriendAddRequestCount();
    }

    /**
     * 好友请求列表
     * @return
     */
    @GetMapping("/apply/pending/list")
    public DeveloperResult<List<NewFriendListDTO>> newFriendList(){
        return friendService.findNewFriendList();
    }

    /**
     * 修改好友请求状态
     * @return
     */
    @PutMapping("/apply-status")
    public DeveloperResult<Boolean> applyStatusModify(){
        return friendService.updateAddFriendRecordStatus();
    }

    /**
     * 是否为好友
     * @param req
     * @return
     */
    @PostMapping("check")
    public DeveloperResult<FriendInfoDTO> isFriend(@RequestBody IsFriendDto req){
        return friendService.isFriend(req);
    }
}
