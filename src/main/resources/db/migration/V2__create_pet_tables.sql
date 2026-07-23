create table breed (
    id bigint not null auto_increment,
    name varchar(255) not null,
    active boolean not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    primary key (id),
    constraint uk_breed_name unique (name)
);

create table pet (
    id bigint not null auto_increment,
    name varchar(255) not null,
    birth_date date not null,
    gender varchar(255) not null,
    breed_id bigint not null,
    neutered_status varchar(255) not null,
    deleted boolean not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    primary key (id),
    constraint fk_pet_breed foreign key (breed_id) references breed (id)
);

create table pet_weight (
    id bigint not null auto_increment,
    pet_id bigint not null,
    weight decimal(10, 1) not null,
    measured_at timestamp(6) not null,
    created_at timestamp(6) not null,
    primary key (id),
    constraint fk_pet_weight_pet foreign key (pet_id) references pet (id)
);

create table pet_membership (
    id bigint not null auto_increment,
    member_id bigint not null,
    pet_id bigint not null,
    role varchar(255) not null,
    status varchar(255) not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    primary key (id),
    constraint uk_pet_membership_member_pet unique (member_id, pet_id),
    constraint fk_pet_membership_member foreign key (member_id) references member (id),
    constraint fk_pet_membership_pet foreign key (pet_id) references pet (id)
);
