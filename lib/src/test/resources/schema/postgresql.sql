drop schema if exists public cascade;
create SCHEMA PUBLIC;
---

create table users (
    id bigint primary key,
    name varchar(100) not null,
    info varchar(200)
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
    date timestamp not null
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
    --bytearray_type bytea not null,
    --bytearray_type_null bytea,
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
    timestamp_type_null timestamp
)
---

alter table clinic       add constraint clinic_fk2       foreign key (city_id)   references city(id);
alter table clinic_users add constraint clinic_users_fk0 foreign key (clinic_id) references clinic(id);
alter table clinic_users add constraint clinic_users_fk1 foreign key (user_id)   references users(id);
alter table user_bills   add constraint user_bills_fk0   foreign key (user_id)   references users(id);
alter table user_bills   add constraint user_bills_fk1   foreign key (bill_id)   references bill(id);
---

insert into users(id, name, info) values(1, 'Joe', null);
insert into users(id, name, info) values(2, 'Donald', 'Do not disturb');

insert into city(id, name) values(1, 'London');
insert into city(id, name) values(2, 'Paris');

insert into clinic(id, name, city_id) values(1, 'Paris Neurology Hospital', 2);
insert into clinic(id, name, city_id) values(2, 'London Heart Hospital', 1);
insert into clinic(id, name, city_id) values(3, 'Diagnostic center', 1);

insert into bill(id, amount, discount, date) values(1, 2000.55, null, '2024-07-01 12:00:00'::timestamp);
insert into bill(id, amount, discount, date) values(2, 1000.20, null, '2024-07-01 16:00:00'::timestamp);
insert into bill(id, amount, discount, date) values(3, 5000, null, '2024-08-01 15:00:00'::timestamp);
insert into bill(id, amount, discount, date) values(4, 7000.77, null, '2024-08-01 15:00:00'::timestamp);
insert into bill(id, amount, discount, date) values(5, 500.10, null, '2024-09-01 15:00:00'::timestamp);

insert into clinic_users values(1, 2);
insert into clinic_users values(2, 1);

insert into user_bills values(1, 1);
insert into user_bills values(1, 2);
insert into user_bills values(2, 3);
insert into user_bills values(2, 4);
insert into user_bills values(2, 5);
---

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