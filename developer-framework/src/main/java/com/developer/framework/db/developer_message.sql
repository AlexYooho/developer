create database developer_message;
use developer_message;
create table group_message
(
    id             bigint auto_increment comment 'id'
        primary key,
    group_id       bigint                               not null comment '群id',
    msg_seq        bigint                               not null comment '群内消息序列号',
    sender_id      bigint                               not null comment '发送用户id',
    sender_nick    varchar(200)                         not null comment '发送时昵称',
    sender_role    int                                  not null comment '发送者角色',
    content_type   tinyint(1)                           not null comment '消息内容类型',
    content        text                                 null comment '发送内容',
    reference_id   bigint                               null comment '引用消息id',
    at_user_ids    varchar(1000)                        null comment '@用户列表',
    status         tinyint(1) default 0                 null comment '状态 0:正常  2:撤回',
    deleted        bit                                  not null comment '删除逻辑',
    like_count     int                                  not null comment '消息点赞数',
    read_count     int                                  not null comment '已读数',
    send_time      datetime(3)                          not null comment '发送时间',
    create_time    datetime(3)                          not null comment '创建时间',
    update_time    datetime(3)                          not null comment '修改时间'
) comment '群消息';

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
    `id`                bigint unsigned NOT NULL COMMENT '雪花ID',
    `uid_a`             bigint unsigned NOT NULL COMMENT '用户a',
    `uid_b`             bigint unsigned NOT NULL COMMENT '用户b',
    `send_id`           bigint not null comment '发送用户id',
    `receiver_id`       bigint not null comment '接收用户id',
    `conv_seq`          bigint unsigned NOT NULL COMMENT '会话序列号',
    `message_content`           text   null COMMENT '发送内容',
    `message_content_type`      tinyint(1) not null COMMENT '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    `message_status`            tinyint(1) not null COMMENT '状态 0:未读 1:已读 2:撤回',
    `read_status`       tinyint(1) not null COMMENT '已读状态 0 未读，1 已读',
    `send_time`         datetime default CURRENT_TIMESTAMP null COMMENT '发送时间',
    `reference_id`      bigint null COMMENT '引用消息id',
    `like_count`        int unsigned NOT NULL DEFAULT 0 COMMENT '点赞数量',
    `extra`             varchar(4000) DEFAULT NULL COMMENT '扩展字段',
    `create_time`       datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`       datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conv_seq` (`uid_a`,`uid_b`,`conv_seq`),
    KEY `idx_conv_time` (`uid_a`,`uid_b`,`send_time` DESC),
    KEY `idx_send` (`send_id`,`send_time` DESC)
) comment '私聊消息';

create table message_conversation
(
    `id`                bigint          not null AUTO_INCREMENT comment '会话全局ID（可选，方便前端引用）',
    `user_id`           bigint          not null comment '会话所属用户ID',
    `conv_type`         tinyint         not null DEFAULT 0 comment '0:私聊、1:群聊、2:系统通知（官方消息）、3:客服会话、4:机器人、5:公众号/订阅号、6:特殊会话（如语音助手）',
    `target_id`         bigint          not null comment '会话对象id',
    `last_msg_seq`      bigint          not null DEFAULT 0 comment '最后一条消息的seq（私聊/群聊用）',
    `last_msg_id`       bigint          default null comment '最后一条消息的id',
    `last_msg_content`  varchar(200)    default null comment '最后消息预览，如“你撤回了一条消息”',
    `last_msg_type`     tinyint         not null default 0 comment '最后一条消息的类型',
    `last_msg_time`     datetime(3)     default null comment '最后活跃时间，用于排序',
    `unread_count`      INT UNSIGNED    not null DEFAULT 0 comment '当前用户的未读消息数',
    `pinned`            tinyint(1)      not null DEFAULT 0 comment '是否置顶',
    `muted`             tinyint(1)      not null DEFAULT 0 comment '是否免打扰',
    `deleted`           tinyint(1)      not null DEFAULT 0 comment '用户是否删除了该会话',
    `draft`             varchar(500)    default null comment '草稿文本',
    `extra`             varchar(500)    default null comment '不同类型会话的专属字段，如群名称、头像、成员数等',
    `create_time`       datetime(3)     not null comment '创建时间',
    `update_time`       datetime(3)     not null comment '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `conv_type`, `target_id`),  -- 一个用户对同一个人/群只有一条会话
    KEY `idx_user_time` (`user_id`, `pinned` DESC, `last_msg_time` DESC),
    KEY `idx_user_updated` (`user_id`, `updated_at` DESC)

)comment='通用会话表（私聊、群聊、系统消息等全部统一管理）';

create table group_message_delete
(
    id             bigint auto_increment comment 'id'
        primary key,
    group_id        bigint                               not null comment '群id',
    msg_id          bigint                               not null comment '群内消息id',
    msg_seq         bigint                               not null comment '群内消息序列号',
    user_id         bigint                               not null comment '谁删了这条消息',
    delete_time     datetime(3)                          not null comment '删除时间',
    create_time     datetime(3)                          not null comment '创建时间',
    update_time     datetime(3)                          not null comment '修改时间'
)comment '群消息删除';
