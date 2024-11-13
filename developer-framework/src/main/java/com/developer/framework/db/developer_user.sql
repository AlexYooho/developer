create database developer_user;
use developer_user;
create table user
(
    id               bigint auto_increment comment 'id'
        primary key,
    account          varchar(50)                             null comment '账号',
    password         varchar(255)                            not null comment '密码(明文)',
    user_name        varchar(255)                            not null comment '用户名',
    nick_name        varchar(255)                            not null comment '用户昵称',
    head_image       varchar(555)  default ''                null comment '用户头像',
    head_image_thumb varchar(555)  default ''                null comment '用户头像缩略图',
    sex              tinyint(1)    default 0                 null comment '性别 0:男 1:女',
    type             smallint      default 1                 null comment '用户类型 1:普通用户 2:审核账户',
    signature        varchar(1024) default ''                null comment '个性签名',
    last_login_time  datetime                                null comment '最后登录时间',
    created_time     datetime      default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '用户表' charset = utf8mb3;

