drop schema public cascade;
create schema public;

create type enum_user_sex as enum('MALE', 'FEMALE');
create type point_nullable as (a int, b int);

create table users (
    id bigserial not null,
    name varchar(100) not null,
    info varchar(200),
    sex enum_user_sex,
    constraint users_pk primary key (id)
);
create table clinic (
    id bigint primary key,
    name varchar(100) not null,
    city_id bigint not null
);
create table city (
    id bigint primary key,
    name varchar(50) not null
);
create table bill (
    id bigint primary key,
    amount decimal(15,2) not null,
    discount decimal(15,2),
    date timestamp with time zone not null
);
create table clinic_users (
    clinic_id bigint not null,
    user_id bigint not null
);
create table user_bills (
    user_id bigint not null,
    bill_id bigint not null
);
create table all_default_data_types(
    bigdecimal_type decimal(15,3) not null,
    bigdecimal_type_null decimal(15,3),
    boolean_type boolean not null,
    boolean_type_null boolean,
    date_type date not null,
    date_type_null date,
    integer_type integer not null,
    integer_type_null integer,
    long_type bigint not null,
    long_type_null bigint,
    string_type varchar(200) not null,
    string_type_null varchar(200),
    short_type smallint not null,
    short_type_null smallint,
    time_type time not null,
    time_type_null time,
    timestamp_type timestamp not null,
    timestamp_type_null timestamp,
    float_type real not null,
    float_type_null real,
    double_type float not null,
    double_type_null float
);

alter table clinic       add constraint clinic_fk2       foreign key (city_id)   references city(id);
alter table clinic_users add constraint clinic_users_fk0 foreign key (clinic_id) references clinic(id);
alter table clinic_users add constraint clinic_users_fk1 foreign key (user_id)   references users(id);
alter table user_bills   add constraint user_bills_fk0   foreign key (user_id)   references users(id);
alter table user_bills   add constraint user_bills_fk1   foreign key (bill_id)   references bill(id);

create procedure digit_magic(in x int, inout y int, out z int)
language plpgsql AS $$ begin
    y = y + x;
    z = y + x;
end; $$;

create procedure bill_zero()
language plpgsql AS $$ begin
    update bill set amount = 0;
end; $$;

create procedure discount_bill(in datedisc timestamp)
language plpgsql AS $$ begin
    update bill set discount = amount * 0.1 where date = datedisc;
end; $$;