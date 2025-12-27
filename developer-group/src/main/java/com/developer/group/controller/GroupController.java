package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.*;
import com.developer.group.dto.group.CreateGroupRequestDTO;
import com.developer.group.dto.group.CreateGroupResponseDTO;
import com.developer.group.dto.group.ModifyGroupInfoRequestDTO;
import com.developer.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("group")
@RequiredArgsConstructor
public class GroupController {


    private final GroupService groupService;

    /**
     * 创建群聊
     * @param req
     * @return
     */
    @PostMapping("/create")
    public DeveloperResult<CreateGroupResponseDTO> createGroup(@RequestBody CreateGroupRequestDTO req){
        return groupService.createGroup(req);
    }

    /**
     * 修改群聊信息
     * @param req
     * @return
     */
    @PutMapping("/modify")
    public DeveloperResult<Boolean> modifyGroup(@RequestBody ModifyGroupInfoRequestDTO req){
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
     * 获取群成员信息列表
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
    public DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(){
        return groupService.findSelfJoinAllGroupInfo();
    }

    /*
    获取和目标用户相同群信息
     */
    @GetMapping("same-group/target/{target_id}")
    public DeveloperResult<List<SameGroupInfoResponseDTO>> getSameGroupInfoList(@PathVariable("target_id") Long targetId){
        return groupService.getSameGroupInfoList(targetId);
    }
}
