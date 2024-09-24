package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.*;
import com.developer.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("group")
public class GroupController {


    @Autowired
    private GroupService groupService;

    /**
     * 创建群聊
     * @param req
     * @return
     */
    @PostMapping("/create")
    public DeveloperResult<CreateGroupRequestDTO> createGroup(@RequestBody CreateGroupRequestDTO req){
        return groupService.createGroup(req);
    }

    /**
     * 修改群聊信息
     * @param req
     * @return
     */
    @PutMapping("/modify")
    public DeveloperResult<CreateGroupRequestDTO> modifyGroup(@RequestBody CreateGroupRequestDTO req){
        return groupService.modifyGroup(req);
    }

    /**
     * 解散群聊
     * @param groupId
     * @return
     */
    @PostMapping("/delete/{groupId}")
    public DeveloperResult<Boolean> deleteGroup(@PathVariable Long groupId){
        return groupService.deleteGroup(groupId);
    }

    /**
     * 查询群聊
     * @param groupId
     * @return
     */
    @GetMapping("/find/{groupId}")
    public DeveloperResult<GroupInfoDTO> findGroup(@PathVariable Long groupId){
        return groupService.findById(groupId);
    }

    /**
     * 查询群聊列表
     * @return
     */
    @GetMapping("/list")
    public DeveloperResult<List<GroupInfoDTO>> findGroups(){
        return groupService.findGroupList();
    }

    /**
     * 邀请进群
     * @param req
     * @return
     */
    @PostMapping("/invite")
    public DeveloperResult<Boolean> invite(@RequestBody GroupInviteRequestDTO req){
        return groupService.invite(req);
    }

    /**
     * 查询群聊成员列表
     * @param groupId
     * @return
     */
    @GetMapping("/{groupId}/members")
    public DeveloperResult<List<GroupMemberDTO>> findGroupMembers(@PathVariable Long groupId){
        return groupService.findGroupMembers(groupId);
    }

    /**
     * 退出群聊
     * @param groupId
     * @return
     */
    @PostMapping("/{groupId}/quit")
    public DeveloperResult<Boolean> quitGroup(@PathVariable Long groupId){
        return groupService.quitGroup(groupId);
    }

    /**
     * 踢出群聊
     * @param groupId
     * @param userId
     * @return
     */
    @PostMapping("/{groupId}/kick/{userId}")
    public DeveloperResult<Boolean> removeGroup(@PathVariable Long groupId, @PathVariable Long userId){
        return groupService.kickGroup(groupId,userId);
    }

    /**
     * 获取当前用户所加入的群信息
     * @return
     */
    @GetMapping("get-self-join-all-group-info")
    public DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(){
        return groupService.findSelfJoinAllGroupInfo();
    }
}
