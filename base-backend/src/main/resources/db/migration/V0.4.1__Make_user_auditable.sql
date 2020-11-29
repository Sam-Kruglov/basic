alter table users
    add last_modified_by bigint;
alter table users
    add last_modified_at timestamp;
alter table users
    add created_by bigint;
alter table users
    add created_at timestamp;

alter table users
    add constraint FK_users_last_modified_by foreign key (last_modified_by) references users;
alter table users
    add constraint FK_users_created_by foreign key (created_by) references users;