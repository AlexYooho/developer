create database developer_message;
use developer_message;
create table group_message
(
    id             bigint auto_increment comment 'id'
        primary key,
    group_id       bigint                               not null comment '群id',
    send_id        bigint                               not null comment '发送用户id',
    content        text                                 null comment '发送内容',
    type           tinyint(1)                           not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    status         tinyint(1) default 0                 null comment '状态 0:正常  2:撤回',
    send_time      datetime   default CURRENT_TIMESTAMP null comment '发送时间',
    send_nick_name varchar(100)                         not null comment '发送用户昵称',
    at_user_ids    varchar(200)                         null comment '@用户列表',
    reference_id   bigint                               null comment '引用消息id',
    like_count     int                                  null comment '消息点赞数'
)
    comment '群消息' charset = utf8mb3;

create index idx_group_id
    on group_message (group_id);

create table group_message_member_receive_record
(
    id          bigint auto_increment comment 'id'
        primary key,
    group_id    bigint      not null comment '群id',
    receiver_id bigint      not null comment '接收者id',
    status      int         not null comment '状态',
    create_time datetime(3) null comment '创建时间',
    update_time datetime(3) null comment '修改时间',
    send_id     bigint      not null comment '发送者id',
    message_id  bigint      null comment '消息id'
)
    comment '群消息成员接收记录表';

create table message_like_record
(
    id           bigint auto_increment comment 'id'
        primary key,
    message_id   bigint      not null comment '消息id',
    message_type int         not null comment '消息类型',
    user_id      bigint      not null comment '点赞用户id',
    like_status  int         not null comment '点赞状态:0-取消点赞,1-点赞',
    like_time    datetime(3) not null comment '点赞时间',
    create_time  datetime(3) not null comment '创建时间',
    update_time  datetime(3) not null comment '修改时间'
)
    comment '消息点赞记录表';

create table private_message
(
    id           bigint auto_increment comment 'id'
        primary key,
    send_id      bigint                             not null comment '发送用户id',
    recv_id      bigint                             not null comment '接收用户id',
    content      text                               null comment '发送内容',
    type         tinyint(1)                         not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    status       tinyint(1)                         not null comment '状态 0:未读 1:已读 2:撤回',
    send_time    datetime default CURRENT_TIMESTAMP null comment '发送时间',
    reference_id bigint                             null comment '引用消息id'
)
    comment '私聊消息' charset = utf8mb3;

create index idx_send_recv_id
    on private_message (send_id, recv_id);

