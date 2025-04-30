create database developer_payment;
use developer_payment;
create table red_packets_info
(
    id               bigint auto_increment comment 'id'
        primary key,
    sender_id        bigint         not null comment '发送者id',
    total_count      int            not null comment '红包个数',
    remaining_count  int            not null comment '剩余红包个数',
    type             int            not null comment '红包类型',
    status           int            not null comment '红包状态',
    message_id       bigint         null comment '关联消息id',
    channel          int            not null comment '红包来源渠道',
    send_amount      decimal(15, 2) not null comment '红包金额',
    return_amount    decimal(15, 2) not null comment '退回金额',
    expire_time      datetime(3)    not null comment '过期时间',
    create_time      datetime(3)    not null comment '创建时间',
    update_time      datetime(3)    not null comment '修改时间',
    remaining_amount decimal(15, 2) not null comment '剩余金额',
    send_time        datetime(3)    not null comment '发送时间'
)
    comment '红包信息';

create table red_packets_receive_detail
(
    id              bigint auto_increment comment 'id'
        primary key,
    red_packets_id  bigint         not null comment '红包id',
    receive_user_id bigint         not null comment '领取用户id',
    receive_amount  decimal(15, 2) not null comment '领取金额',
    receive_time    datetime(3)    null comment '领取时间',
    status          int            not null comment '领取状态',
    create_time     datetime(3)    not null comment '创建时间',
    update_time     datetime(3)    not null comment '修改时间'
)
    comment '红包领取明细';

create table transfer_info
(
    id               bigint auto_increment comment 'id'
        primary key,
    user_id          bigint         not null comment '用户id',
    receiver_user_id bigint         not null comment '接收用户id',
    transfer_amount  decimal(15, 2) not null comment '转账金额',
    transfer_status  int            not null comment '状态',
    create_time      datetime(3)    not null comment '创建时间',
    update_time      datetime(3)    not null comment '修改时间'
)
    comment '转账信息';

create table user_wallet
(
    id                    bigint auto_increment comment 'id'
        primary key,
    user_id               bigint                       not null comment '用户id',
    balance               decimal(15, 2) default 0.00  not null comment '余额',
    frozen_balance        decimal(15, 2) default 0.00  not null comment '冻结余额',
    total_recharge        decimal(15, 2) default 0.00  not null comment '累计重置金额',
    total_withdraw        decimal(15, 2) default 0.00  not null comment '累计提现金额',
    currency              varchar(3)     default 'CNY' not null comment '币种',
    last_transaction_time datetime(3)                  not null comment '最近一次交易时间',
    status                tinyint        default 1     not null comment '钱包状态',
    create_time           datetime(3)                  not null comment '创建时间',
    update_time           datetime(3)                  not null comment '修改时间'
)
    comment '用户钱包';

create table wallet_transaction_record
(
    id               bigint auto_increment comment 'id'
        primary key,
    wallet_id        bigint         not null comment '钱包id',
    user_id          bigint         not null comment '用户id',
    transaction_type int            not null comment '交易类型：0-转账 1-充值 2-体现 3 红包',
    amount           decimal(15, 2) not null comment '交易金额',
    before_balance   decimal(15, 2) not null comment '交易前账户余额',
    after_balance    decimal(15, 2) not null comment '交易后账户余额',
    related_user_id  bigint         null comment '关联用户id,转账收款人id',
    reference_id     varchar(50)    null comment '外部流水编号',
    status           int            not null comment '交易状态',
    created_time     datetime(3)    not null comment '创建时间',
    updated_time     datetime(3)    not null comment '修改时间'
)
    comment '钱包交易表';

create table send_red_packets_message_log(
    id               bigint auto_increment comment 'id'
        primary key,
    red_packets_id   bigint         not null comment '红包id',
    serial_no        varchar(200)   not null comment '操作编号',
    send_status      int            not null comment '消息发送状态0-默认 1-发送成功 2-失败',
    created_time     datetime(3)    not null comment '创建时间',
    updated_time     datetime(3)    not null comment '修改时间',
    remark           varchar(200)   null     comment '备注'
) comment '发送红包消息日志表'

