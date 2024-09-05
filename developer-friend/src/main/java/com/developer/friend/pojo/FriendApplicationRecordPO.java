package com.developer.friend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("friend_application_record")
@NoArgsConstructor
public class FriendApplicationRecordPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主加用户id
     */
    @TableField("main_user_id")
    private Long mainUserId;

    /**
     * 被加用户id
     */
    @TableField("target_user_id")
    private Long targetUserId;

    /**
     * 添加来源
     */
    @TableField("add_mode")
    private Integer addMode;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    public FriendApplicationRecordPO(Long mainUserId, Long targetUserId, Integer addMode, Integer status, Date createTime, Date updateTime, String remark) {
        this.mainUserId = mainUserId;
        this.targetUserId = targetUserId;
        this.addMode = addMode;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.remark = remark;
    }

}
