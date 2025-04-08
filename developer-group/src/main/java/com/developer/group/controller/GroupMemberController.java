package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.BatchModifyGroupMemberInfoRequestDTO;
import com.developer.group.dto.FindGroupMemberUserIdRequestDTO;
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
    @GetMapping("/get-group-member-user-id")
    public DeveloperResult<List<Long>> findGroupMemberUserId(@RequestBody FindGroupMemberUserIdRequestDTO req){
        return groupMemberService.findGroupMember(req);
    }

    @PutMapping("update/list")
    public DeveloperResult<Boolean> batchModifyGroupMemberInfo(@RequestBody BatchModifyGroupMemberInfoRequestDTO req){
        return groupMemberService.batchModifyGroupMemberInfo(req);
    }

}
