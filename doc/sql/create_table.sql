-- auto-generated definition
create schema project collate utf8mb4_general_ci;

grant alter, alter routine, create, create routine, create temporary tables, create view, delete, drop, event, execute, index, insert, lock tables, references, select, show view, trigger, update on project.* to project;

create table user
(
    id        int auto_increment comment 'id'
        primary key,
    user_name varchar(255) null comment '用户名'
)
    comment '用户';

create table permission
(
    id              int auto_increment
        primary key,
    permission_name varchar(64) not null comment '权限名称',
    constraint permission_permission_name_uindex
        unique (permission_name)
)
    comment '权限';

create table user_permission
(
    id            int auto_increment
        primary key,
    user_id       int not null,
    permission_id int not null
)
    comment '用户权限';

# both set password 123456
INSERT INTO user (id, user_name, password) VALUES (1, 'demo', '$2a$10$KrCaa8a0Hcig8kRWXYHakubmIjQCaE7SKU.Qwd/7gMnjiXWC1DT0i');
INSERT INTO project.user (id, user_name, password) VALUES (2, 'demo1', '$2a$10$GQkKx551nr7wQNwt2JTfjO5ayKCoFVpyHvNiyHXbG3iXxBd1GrP8q');

INSERT INTO permission (id, permission_name) VALUES (1, 'permission1');
INSERT INTO permission (id, permission_name) VALUES (2, 'permission2');

INSERT INTO user_permission (id, user_id, permission_id) VALUES (1, 1, 1);
INSERT INTO user_permission (id, user_id, permission_id) VALUES (2, 1, 2);
INSERT INTO user_permission (id, user_id, permission_id) VALUES (3, 2, 2);


