create table member (
    id bigint not null auto_increment,
    email varchar(255) not null,
    password varchar(255) not null,
    status varchar(255) not null,
    deleted boolean not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    deleted_at timestamp(6),
    primary key (id),
    constraint uk_member_email unique (email)
);
