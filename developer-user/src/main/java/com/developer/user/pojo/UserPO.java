package com.developer.user.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class UserPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "account")
    private String account;

    @TableField(value = "password")
    private String password;

    @TableField(value = "user_name")
    private String username;

    @TableField(value = "nick_name")
    private String nickname;

    @TableField(value = "head_image")
    private String headImage;

    @TableField(value = "head_image_thumb")
    private String headImageThumb;

    @TableField(value = "sex")
    private Integer sex;

    @TableField(value = "type")
    private Integer type;

    @TableField(value = "email")
    private String email;

    @TableField("area")
    private String area;

    @TableField(value = "signature")
    private String signature;

    @TableField(value = "last_login_time")
    private Date lastLoginTime;

    @TableField(value = "created_time")
    private Date createTime;

    public UserPO(String account, String username, String nickname, String headImage, String headImageThumb, String password, Integer sex, Integer type,String email, String signature, Date lastLoginTime, Date createTime) {
        this.account = account;
        this.username = username;
        this.nickname = nickname;
        this.headImage = headImage;
        this.headImageThumb = headImageThumb;
        this.password = password;
        this.sex = sex;
        this.type = type;
        this.email = email;
        this.signature = signature;
        this.lastLoginTime = lastLoginTime;
        this.createTime = createTime;
    }
}
