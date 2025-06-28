package com.developer.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GroupMessageDTO extends SendMessageResultDTO {

//    private Long groupId;
//
//    private List<Long> atUserIds;
//
//    private Long unReadCount;
//
//    private Long readCount;

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;

    @JsonProperty("un_read_count")
    private Long unReadCount;

    @JsonProperty("read_count")
    private Long readCount;
}
