package com.developer.im.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class GroupMessageDTO {

    /**
     * 群id
     */
    private Long groupId;

    /**
     * 接收者id集合
     */
    private List<Long> receiverIds = Collections.EMPTY_LIST;

    /**
     * at用户id集合
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;
}
