package com.developer.user.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_heartbeat")
public class UserHeartbeatPO {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 心跳状态
     */
    @TableField("heartbeat_status")
    private Boolean heartbeatStatus;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private Date LoginTime;

    /**
     * 离线时间
     */
    @TableField("offline_time")
    private Date offlineTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date update_time;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
