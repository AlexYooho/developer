create datebase developer_group;

create table group_info
(
    id               bigint auto_increment comment 'id'
        primary key,
    name             varchar(255)                            not null comment '群名字',
    owner_id         bigint                                  not null comment '群主id',
    head_image       varchar(500)  default ''                null comment '群头像',
    head_image_thumb varchar(500)  default ''                null comment '群头像缩略图',
    notice           varchar(1024) default ''                null comment '群公告',
    remark           varchar(255)  default ''                null comment '群备注',
    deleted          tinyint(1)    default 0                 null comment '是否已删除',
    created_time     datetime      default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '群信息' charset = utf8mb3;

create table group_member
(
    id           bigint auto_increment comment 'id'
        primary key,
    group_id     bigint                                 not null comment '群id',
    user_id      bigint                                 not null comment '用户id',
    alias_name   varchar(255) default ''                null comment '组内显示名称',
    head_image   varchar(555) default ''                null comment '用户头像',
    remark       varchar(255) default ''                null comment '备注',
    quit         tinyint(1)   default 0                 null comment '是否已退出',
    created_time datetime     default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '群成员' charset = utf8mb3;

create index idx_group_id
    on group_member (group_id);

create index idx_user_id
    on group_member (user_id);

