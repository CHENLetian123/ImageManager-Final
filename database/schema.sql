drop database if exists imagemanager_final;

create database imagemanager_final
    default character set utf8mb4
    collate utf8mb4_general_ci;

use imagemanager_final;

create table users (
    id bigint primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    created_at datetime
) engine=InnoDB default charset=utf8mb4;

create table images (
    id bigint primary key auto_increment,
    user_id bigint not null,
    title varchar(150),
    description text,
    category varchar(80),
    tags varchar(300),
    source_type varchar(30),
    source_name varchar(500),
    original_file_name varchar(255),
    original_path varchar(800),
    relative_path varchar(800),
    r2_object_key varchar(800),
    public_url varchar(1000),
    content_type varchar(100),
    file_size bigint,
    last_modified bigint,
    created_at datetime,
    updated_at datetime,
    index idx_user_id(user_id),
    index idx_source_name(source_name),
    index idx_last_modified(last_modified)
) engine=InnoDB default charset=utf8mb4;
