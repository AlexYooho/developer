create database developer_friend;
use developer_friend;
create table friend_info
(
    id                bigint auto_increment comment 'id'
        primary key,
    user_id           bigint                                 not null comment '用户id',
    friend_id         bigint                                 not null comment '好友id',
    alias             varchar(100)                           null comment '好友备注名',
    tag_name          varchar(50)                            null comment '标签',
    status            tinyint                                not null comment '关系状态：0=正常、1=删除、2=拉黑',
    add_source        tinyint                                not null comment '添加来源：0=未知、1=搜索添加、2=群聊添加',
    friend_nick_name  varchar(255)                           not null comment '好友昵称',
    friend_head_image varchar(555)                           null comment '好友头像',
    create_time       datetime(3)                            not null comment '创建时间',
    update_time       datetime(3)                            not null comment '修改时间',
    remark            varchar(100)                           not null comment '备注'
)
    comment '好友信息' charset = utf8mb3;

create index idx_friend_id
    on friend (friend_id);

create index idx_user_id
    on friend (user_id);

