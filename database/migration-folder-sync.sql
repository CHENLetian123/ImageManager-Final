-- Folder Sync migration for an existing ImageManager Final database.
-- Run this against Aiven MySQL defaultdb. This file does not drop data.

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

set @column_exists := (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'images'
      and column_name = 'folder_preset_id'
);
set @sql := if(
    @column_exists = 0,
    'alter table images add column folder_preset_id bigint null',
    'select ''folder_preset_id already exists'' as message'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @index_exists := (
    select count(*)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'images'
      and index_name = 'idx_images_folder_preset'
);
set @sql := if(
    @index_exists = 0,
    'create index idx_images_folder_preset on images(folder_preset_id)',
    'select ''idx_images_folder_preset already exists'' as message'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @index_exists := (
    select count(*)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'images'
      and index_name = 'idx_images_relative_path'
);
set @sql := if(
    @index_exists = 0,
    'create index idx_images_relative_path on images(relative_path(255))',
    'select ''idx_images_relative_path already exists'' as message'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @index_exists := (
    select count(*)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'images'
      and index_name = 'uk_user_preset_relative'
);
set @sql := if(
    @index_exists = 0,
    'alter table images add unique key uk_user_preset_relative(user_id, folder_preset_id, relative_path(255))',
    'select ''uk_user_preset_relative already exists'' as message'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;
