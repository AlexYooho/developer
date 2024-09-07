package com.developer.group.controller;

import com.developer.framework.model.DeveloperResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("group-member")
public class GroupMemberController {


    @GetMapping("get-self-groups")
    public DeveloperResult findSelfAllGroup(){

        return DeveloperResult.success();
    }

}
