-- ImageManager Final schema.
-- For Aiven MySQL, connect to the existing database named defaultdb before running this file.

create table if not exists users (
    id bigint primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    created_at datetime
) engine=InnoDB default charset=utf8mb4;

create table if not exists folder_presets (
    id bigint primary key auto_increment,
    user_id bigint not null,
    name varchar(100) not null,
    source_path varchar(800),
    created_at datetime,
    updated_at datetime,
    last_sync_at datetime,
    index idx_folder_user_id(user_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists images (
    id bigint primary key auto_increment,
    user_id bigint not null,
    folder_preset_id bigint null,
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
    index idx_last_modified(last_modified),
    index idx_images_folder_preset(folder_preset_id),
    index idx_images_relative_path(relative_path(255)),
    unique key uk_user_preset_relative(user_id, folder_preset_id, relative_path(255))
) engine=InnoDB default charset=utf8mb4;
