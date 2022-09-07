-- auto-generated definition
create schema xqcloud collate utf8mb4_general_ci;

drop table if exists user;
drop table if exists permission;
drop table if exists user_permission;

create table user
(
    id          int unsigned auto_increment comment '自增id'
        primary key,
    name        varchar(128)                               not null comment '登录用户名',
    password    varchar(128)                               not null comment '密码',
    email       varchar(255)     default ''                null comment '邮箱',
    phone       varchar(20)      default ''                null comment '手机',
    nick_name   varchar(255)     default ''                null comment '昵称',
    avatar      varchar(255)     default ''                null comment '头像',
    size_max    bigint unsigned  default 0                 not null comment '储存空间大小（byte） 0-不限制',
    size_use    bigint unsigned  default 0                 not null comment '已使用大小（byte）',
    status      tinyint unsigned default 1                 not null comment '用户启用状态 0-未启用 1-启用',
    last_login  timestamp                                  null comment '最后登陆时间',
    modify_time timestamp                                  null comment '最后修改时间',
    create_time timestamp        default CURRENT_TIMESTAMP null comment '创建时间',
    constraint user_name_uindex
        unique (name)
)
    comment '用户表';

create table permission
(
    id          int unsigned auto_increment comment '自增id'
        primary key,
    name        varchar(128)                        not null comment '权限名',
    modify_time timestamp                           null comment '最后修改时间',
    create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    constraint permission_name_uindex
        unique (name)
)
    comment '权限表';

create table user_permission
(
    id            int unsigned auto_increment comment '自增id'
        primary key,
    user_id       int unsigned null comment '用户id',
    permission_id int unsigned null comment '权限id',
    constraint user_permission_user_id_permission_id_uindex
        unique (user_id, permission_id)
)
    comment '用户权限对应表';

# both set password 123456
INSERT INTO user (name, password, size_max) VALUES ('demo0', '$2a$10$KrCaa8a0Hcig8kRWXYHakubmIjQCaE7SKU.Qwd/7gMnjiXWC1DT0i', 21474836480);
INSERT INTO user (name, password) VALUES ('demo1', '$2a$10$GQkKx551nr7wQNwt2JTfjO5ayKCoFVpyHvNiyHXbG3iXxBd1GrP8q');

INSERT INTO permission (name) VALUES ('read');
INSERT INTO permission (name) VALUES ('write');

INSERT INTO user_permission (user_id, permission_id) VALUES (1, 1);
INSERT INTO user_permission (user_id, permission_id) VALUES (1, 2);
INSERT INTO user_permission (user_id, permission_id) VALUES (2, 1);




