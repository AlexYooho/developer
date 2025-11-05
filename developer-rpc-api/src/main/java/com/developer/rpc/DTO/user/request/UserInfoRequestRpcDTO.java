package com.developer.rpc.DTO.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserInfoRequestRpcDTO implements Serializable {

    //@JsonProperty("user_ids")
    private List<Long> userIds;

}
