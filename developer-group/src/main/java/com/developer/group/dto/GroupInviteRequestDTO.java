package com.developer.group.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupInviteRequestDTO {

    private Long groupId;

    private List<Long> friendIds;


}
