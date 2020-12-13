-- index starts with "id_"
-- unique constraint starts with "un_"
-- foreign key constraint starts with "fk_"

create sequence user_seq;
create table users(
	id bigint primary key,
	email varchar_ignorecase(70) not null,
	encoded_password varchar_ignorecase(500) not null,
	first_name varchar(255) not null,
	last_name varchar(255) not null,
	last_modified_by bigint,
	last_modified_at timestamp not null,
	created_by bigint,
	created_at timestamp not null,
	constraint fk_users_created_by foreign key (created_by) references users (id),
	constraint fk_users_last_modified_by foreign key (last_modified_by) references users (id)
);
create unique index id_un_users_email on users (email);

create table roles(
	id integer primary key,
	name varchar(255) not null
);
create unique index id_un_roles_name on roles (name);

create table users_to_roles(
	user_id bigint not null,
	role_id integer not null,
	constraint fk_users_to_roles_user_id foreign key (user_id) references users (id),
	constraint fk_users_to_roles_role_id foreign key (role_id) references roles (id)
);
create index id_users_to_roles_user_id on users_to_roles (user_id);

-- Essential data
insert into roles(id, name) values (1, 'ADMIN'), (2, 'USER');