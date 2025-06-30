create table users (
    id bigserial not null,
    name varchar(100) not null,
    info varchar(200),
    constraint users_pk primary key (id)
);