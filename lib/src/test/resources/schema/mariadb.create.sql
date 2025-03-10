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

create function f_boolean(x boolean)
    RETURNS boolean DETERMINISTIC RETURN x;
create procedure p_boolean(in x boolean, out y boolean)
    begin set y = x; end;

create function f_tinyint(x tinyint)
    RETURNS tinyint DETERMINISTIC RETURN x;
create procedure p_tinyint(in x tinyint, out y tinyint)
    begin set y = x; end;

create function f_smallint(x smallint)
    RETURNS smallint DETERMINISTIC RETURN x;
create procedure p_smallint(in x smallint, out y smallint)
    begin set y = x; end;

create function f_int(x int)
    RETURNS int DETERMINISTIC RETURN x;
create procedure p_int(in x int, out y int)
    begin set y = x; end;

create function f_bigint(x bigint)
    RETURNS bigint DETERMINISTIC RETURN x;
create procedure p_bigint(in x bigint, out y bigint)
    begin set y = x; end;

create function f_float(x float)
    RETURNS float DETERMINISTIC RETURN x;
create procedure p_float(in x float, out y float)
    begin set y = x; end;

create function f_double(x double)
    RETURNS double DETERMINISTIC RETURN x;
create procedure p_double(in x double, out y double)
    begin set y = x; end;

create procedure p_decimal(in x decimal(15, 3), out y decimal(15, 3))
    begin set y = x; end;

create function f_varchar(x varchar(100))
    RETURNS varchar(100) DETERMINISTIC RETURN x;
create procedure p_varchar(in x varchar(100), out y varchar(100))
    begin set y = x; end;

create function f_date(x date)
    RETURNS date DETERMINISTIC RETURN x;
create procedure p_date(in x date, out y date)
    begin set y = x; end;

create function f_time(x time)
    RETURNS time DETERMINISTIC RETURN x;
create procedure p_time(in x time, out y time)
    begin set y = x; end;

create function f_datetime(x datetime)
    RETURNS datetime DETERMINISTIC RETURN x;
create procedure p_datetime(in x datetime, out y datetime)
    begin set y = x; end;

create function f_varbinary(x varbinary(32))
    RETURNS varbinary(32) DETERMINISTIC RETURN x;
create procedure p_varbinary(in x varbinary(32), out y varbinary(32))
    begin set y = x; end;

create function f_uuid(x uuid)
    RETURNS uuid DETERMINISTIC RETURN x;
create procedure p_uuid(in x uuid, out y uuid)
    begin set y = x; end;