package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public DeveloperResult findGroupMemberUserId(@PathVariable("groupId") Long groupId){
        return groupMemberService.findGroupMember(groupId);
    }

}
