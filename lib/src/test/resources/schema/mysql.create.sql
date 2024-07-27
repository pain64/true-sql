drop database test;
create database test;
use test;

create table if not exists users (
    id bigint not null AUTO_INCREMENT,
    name varchar(100) not null,
    info varchar(200),
    constraint users_pk PRIMARY KEY(id)
);
create table if not exists city (
    id bigint not null PRIMARY KEY,
    name varchar(50) not null
);
create table if not exists clinic (
    id bigint not null PRIMARY KEY,
    name varchar(100) not null,
    city_id bigint not null,
    constraint clinic_fk2       foreign key (city_id)   references city(id)
);
create table if not exists bill (
    id bigint not null PRIMARY KEY,
    amount decimal(15,2) not null,
    discount decimal(15,2),
    date datetime not null
);
create table if not exists clinic_users (
    clinic_id bigint not null,
    user_id bigint not null,
    constraint clinic_users_fk0 foreign key (clinic_id) references clinic(id),
    constraint clinic_users_fk1 foreign key (user_id)   references users(id)
);
create table if not exists user_bills (
    user_id bigint not null,
    bill_id bigint not null,
    constraint user_bills_fk0   foreign key (user_id)   references users(id),
    constraint user_bills_fk1   foreign key (bill_id)   references bill(id)
);

create table if not exists all_default_data_types(
    byte_type tinyint not null,
    byte_type_null tinyint
);

create procedure if not exists digit_magic(in x int, inout y int, out z int)
    begin
        set y = y + x;
        set z = y + x;
    end;

create procedure if not exists bill_zero()
modifies sql data
    begin
        update bill set amount = 0;
    end;

create procedure if not exists discount_bill(in datedisc datetime)
modifies sql data
    begin
        update bill set discount = amount * 0.1 where date = datedisc;
    end;

create procedure if not exists test_types_procedure(
   inout bigdecimal_type decimal(15,3),
   out bigdecimal_type_null decimal(15,3),
   inout boolean_type boolean,
   out boolean_type_null boolean,
   -- bytearray_type bytea not null,
   -- bytearray_type_null bytea,
   inout date_type date,
   out date_type_null date,
   inout integer_type integer,
   out integer_type_null integer,
   inout long_type bigint,
   out long_type_null bigint,
   inout string_type varchar(200),
   out string_type_null varchar(200),
   inout short_type smallint,
   out short_type_null smallint,
   inout byte_type tinyint,
   out byte_type_null tinyint,
   inout time_type time,
   out time_type_null time,
   inout timestamp_type datetime,
   out timestamp_type_null datetime
)
   begin
       declare b boolean;
       set b = true;
   end;
