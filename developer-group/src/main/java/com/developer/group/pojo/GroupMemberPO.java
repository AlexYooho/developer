package com.developer.group.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("group_member")
public class GroupMemberPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     *  群内显示名称
     */
    @TableField("alias_name")
    private String aliasName;

    /**
     *  头像
     */
    @TableField("head_image")
    private String headImage;



    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已离开群聊
     */
    @TableField("quit")
    private Boolean quit;


    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

}
