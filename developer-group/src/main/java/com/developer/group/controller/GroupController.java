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
     * @param req
     * @return
     */
    @PostMapping("/delete")
    public DeveloperResult<Boolean> deleteGroup(@RequestBody DissolveGroupRequestDTO req){
        return groupService.deleteGroup(req);
    }

    /**
     * 查询群聊
     * @param req
     * @return
     */
    @PostMapping("/find")
    public DeveloperResult<GroupInfoDTO> findGroup(@RequestBody FindGroupRequestDTO req){
        return groupService.findById(req);
    }

    /**
     * 查询群聊列表
     * @return
     */
    @GetMapping("/list")
    public DeveloperResult<List<GroupInfoDTO>> findGroups(@RequestParam("serial_no") String serialNo){
        return groupService.findGroupList(serialNo);
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
     * @param req
     * @return
     */
    @GetMapping("/members")
    public DeveloperResult<List<GroupMemberDTO>> findGroupMembers(@RequestBody FindGroupMembersRequestDTO req){
        return groupService.findGroupMembers(req);
    }

    /**
     * 退出群聊
     * @param req
     * @return
     */
    @PostMapping("/quit")
    public DeveloperResult<Boolean> quitGroup(@RequestBody QuitGroupRequestDTO req){
        return groupService.quitGroup(req);
    }

    /**
     * 踢出群聊
     * @param req
     * @return
     */
    @PostMapping("/kick")
    public DeveloperResult<Boolean> kickOutGroup(@RequestBody KickOutGroupRequestDTO req){
        return groupService.kickGroup(req);
    }

    /**
     * 获取当前用户所加入的群信息
     * @return
     */
    @GetMapping("get-self-join-all-group-info")
    public DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(@RequestParam("serial_no") String serialNo){
        return groupService.findSelfJoinAllGroupInfo(serialNo);
    }
}
