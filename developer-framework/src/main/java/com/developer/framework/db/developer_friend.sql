create database developer_friend;
use developer_friend;
create table friend
(
    id                bigint auto_increment comment 'id'
        primary key,
    user_id           bigint                                 not null comment '用户id',
    friend_id         bigint                                 not null comment '好友id',
    friend_nick_name  varchar(255)                           not null comment '好友昵称',
    friend_head_image varchar(555) default ''                null comment '好友头像',
    created_time      datetime     default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '好友' charset = utf8mb3;

create index idx_friend_id
    on friend (friend_id);

create index idx_user_id
    on friend (user_id);

