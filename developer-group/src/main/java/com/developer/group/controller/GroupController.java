package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.CreateGroupRequestDTO;
import com.developer.group.dto.GroupInviteRequestDTO;
import com.developer.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public DeveloperResult createGroup(@RequestBody CreateGroupRequestDTO req){
        return groupService.createGroup(req);
    }

    /**
     * 修改群聊信息
     * @param req
     * @return
     */
    @PutMapping("/modify")
    public DeveloperResult modifyGroup(@RequestBody CreateGroupRequestDTO req){
        return groupService.modifyGroup(req);
    }

    /**
     * 解散群聊
     * @param groupId
     * @return
     */
    @PostMapping("/delete/{groupId}")
    public DeveloperResult deleteGroup(@PathVariable Long groupId){
        return groupService.deleteGroup(groupId);
    }

    /**
     * 查询群聊
     * @param groupId
     * @return
     */
    @GetMapping("/find/{groupId}")
    public DeveloperResult findGroup(@PathVariable Long groupId){
        return groupService.findById(groupId);
    }

    /**
     * 查询群聊列表
     * @return
     */
    @GetMapping("/list")
    public DeveloperResult findGroups(){
        return groupService.findGroupList();
    }

    /**
     * 邀请进群
     * @param req
     * @return
     */
    @PostMapping("/invite")
    public DeveloperResult invite(@RequestBody GroupInviteRequestDTO req){
        return groupService.invite(req);
    }

    /**
     * 查询群聊成员列表
     * @param groupId
     * @return
     */
    @GetMapping("/{groupId}/members")
    public DeveloperResult findGroupMembers(@PathVariable Long groupId){
        return groupService.findGroupMembers(groupId);
    }

    /**
     * 退出群聊
     * @param groupId
     * @return
     */
    @PostMapping("/{groupId}/quit")
    public DeveloperResult quitGroup(@PathVariable Long groupId){
        return groupService.quitGroup(groupId);
    }

    /**
     * 踢出群聊
     * @param groupId
     * @param userId
     * @return
     */
    @PostMapping("/{groupId}/kick/{userId}")
    public DeveloperResult removeGroup(@PathVariable Long groupId, @PathVariable Long userId){
        return groupService.kickGroup(groupId,userId);
    }



}
