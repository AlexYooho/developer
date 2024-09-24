package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.SelfJoinGroupInfoDTO;
import com.developer.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("group-member")
public class GroupMemberController {

    @Autowired
    private GroupMemberService groupMemberService;

    /**
     * 获取群成员id
     * @return
     */
    @GetMapping("/group/{groupId}/get-group-member-user-id")
    public DeveloperResult<List<Long>> findGroupMemberUserId(@PathVariable("groupId") Long groupId){
        return groupMemberService.findGroupMember(groupId);
    }

    @PutMapping("update/list")
    public DeveloperResult<Boolean> batchModifyGroupMemberInfo(@RequestBody List<SelfJoinGroupInfoDTO> list){
        return groupMemberService.batchModifyGroupMemberInfo(list);
    }

}
